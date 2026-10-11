package com.homredi.inspection;

import com.homredi.unit.Unit;
import com.homredi.unit.UnitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({CheckoutInspectionService.class, InspectionTaskService.class})
@Testcontainers
class CheckoutPersistenceIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private CheckoutInspectionService checkoutInspectionService;

    @Autowired
    private InspectionTaskService inspectionTaskService;

    @Autowired
    private UnitRepository units;

    @Autowired
    private PostCheckoutInspectionRepository inspections;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void startInspection_persistsInspectionTasksAndUnitStatus_whenCheckoutIsInitiated() {
        Unit unit = units.save(new Unit(null, "Unit 1", "Lagos", List.of("Bedroom", "Kitchen"),
                Unit.TurnoverStatus.OCCUPIED));
        String checkoutEventId = "checkout-event-1";

        PostCheckoutInspection startedInspection = checkoutInspectionService.startInspection(unit.getId(), checkoutEventId);

        PostCheckoutInspection storedInspection = inspections
                .findByUnitIdAndCheckoutEventId(unit.getId(), checkoutEventId)
                .orElseThrow();
        Unit storedUnit = units.findById(unit.getId()).orElseThrow();

        assertThat(startedInspection.getId()).isNotNull();
        assertThat(storedInspection.getStatus()).isEqualTo(PostCheckoutInspection.Status.PENDING);
        assertThat(storedInspection.getTasks()).hasSize(2)
                .extracting(Task::getDescription)
                .containsExactlyInAnyOrder("Bedroom task", "Kitchen task");
        assertThat(storedInspection.getTasks()).extracting(Task::getState)
                .containsOnly(Task.State.PENDING);
        assertThat(storedUnit.getTurnoverStatus()).isEqualTo(Unit.TurnoverStatus.AWAITING_INSPECTION);
    }

    @Test
    void startInspection_returnsExistingInspectionWithoutDuplicatingTasks_whenCheckoutEventIsRetried() {
        Unit unit = units.save(new Unit(null, "Unit 2", "Lagos", List.of("Bedroom", "Kitchen"),
                Unit.TurnoverStatus.OCCUPIED));
        String checkoutEventId = "checkout-event-retry";

        PostCheckoutInspection firstResult = checkoutInspectionService.startInspection(unit.getId(), checkoutEventId);
        PostCheckoutInspection retryResult = checkoutInspectionService.startInspection(unit.getId(), checkoutEventId);

        assertThat(retryResult.getId()).isEqualTo(firstResult.getId());
        assertThat(inspections.count()).isEqualTo(1);
        assertThat(inspections.findById(firstResult.getId()).orElseThrow().getTasks()).hasSize(2);
        assertThat(units.findById(unit.getId()).orElseThrow().getTurnoverStatus())
                .isEqualTo(Unit.TurnoverStatus.AWAITING_INSPECTION);
    }

    @Test
    void saveInspection_persistsCheckedTask_whenCaretakerChecksTask() {
        Unit unit = units.save(new Unit(null, "Unit 3", "Lagos", List.of("Bedroom", "Kitchen"),
                Unit.TurnoverStatus.OCCUPIED));
        PostCheckoutInspection createdInspection = checkoutInspectionService.startInspection(unit.getId(), "checkout-event-task");
        PostCheckoutInspection inspection = inspections.findById(createdInspection.getId()).orElseThrow();
        Task taskToCheck = inspection.getTasks().iterator().next();

        inspection.checkTask(taskToCheck);
        inspections.save(inspection);

        PostCheckoutInspection reloadedInspection = inspections.findById(inspection.getId()).orElseThrow();
        assertThat(reloadedInspection.getTasks())
                .filteredOn(task -> task.getId().equals(taskToCheck.getId()))
                .extracting(Task::getState)
                .containsExactly(Task.State.PASSED);
        assertThat(reloadedInspection.getTasks())
                .filteredOn(task -> !task.getId().equals(taskToCheck.getId()))
                .extracting(Task::getState)
                .containsOnly(Task.State.PENDING);
    }

    @Test
    void checkTask_persistsPassedState_whenCaretakerChecksTask() {
        Unit unit = units.save(new Unit(null, "Unit 4", "Lagos", List.of("Bedroom", "Kitchen"),
                Unit.TurnoverStatus.OCCUPIED));
        PostCheckoutInspection inspection = checkoutInspectionService.startInspection(unit.getId(), "checkout-event-command");
        Long taskId = inspection.getTasks().iterator().next().getId();

        inspectionTaskService.checkTask(inspection.getId(), taskId);

        PostCheckoutInspection reloadedInspection = inspections.findById(inspection.getId()).orElseThrow();
        assertThat(reloadedInspection.getTasks())
                .filteredOn(task -> task.getId().equals(taskId))
                .extracting(Task::getState)
                .containsExactly(Task.State.PASSED);
        assertThat(reloadedInspection.getTasks())
                .filteredOn(task -> !task.getId().equals(taskId))
                .extracting(Task::getState)
                .containsOnly(Task.State.PENDING);
    }

    @Test
    void checkTask_rejectsTaskFromAnotherInspection() {
        Unit firstUnit = units.save(new Unit(null, "Unit 5", "Lagos", List.of("Bedroom"),
                Unit.TurnoverStatus.OCCUPIED));
        Unit secondUnit = units.save(new Unit(null, "Unit 6", "Lagos", List.of("Kitchen"),
                Unit.TurnoverStatus.OCCUPIED));
        PostCheckoutInspection firstInspection = checkoutInspectionService.startInspection(firstUnit.getId(), "checkout-event-first");
        PostCheckoutInspection secondInspection = checkoutInspectionService.startInspection(secondUnit.getId(), "checkout-event-second");
        Long taskFromSecondInspection = secondInspection.getTasks().iterator().next().getId();

        assertThatThrownBy(() -> inspectionTaskService.checkTask(firstInspection.getId(), taskFromSecondInspection))
                .isInstanceOf(java.util.NoSuchElementException.class);

        PostCheckoutInspection reloadedSecondInspection = inspections.findById(secondInspection.getId()).orElseThrow();
        assertThat(reloadedSecondInspection.getTasks())
                .extracting(Task::getState)
                .containsOnly(Task.State.PENDING);
    }

    @Test
    void uncheckTask_persistsPendingState_whenCaretakerUnchecksPassedTask() {
        Unit unit = units.save(new Unit(null, "Unit 7", "Lagos", List.of("Bedroom", "Kitchen"),
                Unit.TurnoverStatus.OCCUPIED));
        PostCheckoutInspection inspection = checkoutInspectionService.startInspection(unit.getId(), "checkout-event-uncheck");
        Long taskId = inspection.getTasks().iterator().next().getId();
        inspectionTaskService.checkTask(inspection.getId(), taskId);

        inspectionTaskService.uncheckTask(inspection.getId(), taskId);

        PostCheckoutInspection reloadedInspection = inspections.findById(inspection.getId()).orElseThrow();
        assertThat(reloadedInspection.getTasks())
                .filteredOn(task -> task.getId().equals(taskId))
                .extracting(Task::getState)
                .containsExactly(Task.State.PENDING);
        assertThat(reloadedInspection.getTasks())
                .filteredOn(task -> !task.getId().equals(taskId))
                .extracting(Task::getState)
                .containsOnly(Task.State.PENDING);
    }
}
