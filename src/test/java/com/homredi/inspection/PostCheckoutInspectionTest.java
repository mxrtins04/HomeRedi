package com.homredi.inspection;

import org.junit.jupiter.api.Test;

import com.homredi.inspection.PostCheckoutInspection;
import com.homredi.unit.Unit;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostCheckoutInspectionTest {

    private static final Long unitId = 1L;

    @Test
    void checkout_startsPendingInspection_whenCheckoutIsInitiated() {
        Unit unit = new Unit(unitId, "Test unit", "Lagos", List.of("Bedroom", "Kitchen"), Unit.TurnoverStatus.OCCUPIED);

        PostCheckoutInspection inspection = PostCheckoutInspection.checkout(unit, "test-checkout");

        assertThat(inspection.getUnitId()).isEqualTo(unitId);
        assertThat(inspection.getStatus()).isEqualTo(PostCheckoutInspection.Status.PENDING);
    }

    @Test
    void checkout_createsOneTaskPerSpace_whenCheckoutIsInitiated() {
        Unit unit = new Unit(unitId, "Test unit", "Lagos", List.of("Bedroom", "Kitchen", "Parlour"), Unit.TurnoverStatus.OCCUPIED);

        PostCheckoutInspection inspection = PostCheckoutInspection.checkout(unit, "test-checkout");
        int numberOfTasks = inspection.getTasks().size();

        assertThat(numberOfTasks).isEqualTo(3);
    }

    @Test
    void checkout_setsTaskStatusToPending_whenCheckoutIsInitiated() {
        Unit unit = new Unit(unitId, "Test unit", "Lagos", List.of("Bedroom"), Unit.TurnoverStatus.OCCUPIED);

        PostCheckoutInspection inspection = PostCheckoutInspection.checkout(unit, "test-checkout");

        Task task = inspection.getTasks().iterator().next();


        assertThat(task.getState()).isEqualTo(Task.State.PENDING);
    }

    @Test
    void checkout_createsTaskForEachSpace_whenCheckoutIsInitiated() {
        Unit unit = new Unit(unitId, "Test unit", "Lagos", List.of("Bedroom", "Kitchen", "Parlour"), Unit.TurnoverStatus.OCCUPIED);
        PostCheckoutInspection inspection = PostCheckoutInspection.checkout(unit, "test-checkout");
        Set<Task> tasks = inspection.getTasks();

        assertThat(tasks).extracting(Task::getDescription)
                .containsExactlyInAnyOrder("Bedroom task", "Kitchen task", "Parlour task");
    }

    @Test
    void complete_throwsException_whenAnyTaskIsPending() {
        Unit unit = new Unit(unitId, "Test unit", "Lagos", List.of("Bedroom", "Kitchen"), Unit.TurnoverStatus.OCCUPIED);
        PostCheckoutInspection inspection = PostCheckoutInspection.checkout(unit, "test-checkout");
        inspection.checkTask(inspection.getTasks().iterator().next());

        assertThatThrownBy(() -> inspection.complete(unit))
                .isInstanceOf(IllegalStateException.class);
        assertThat(inspection.getStatus()).isEqualTo(PostCheckoutInspection.Status.PENDING);
        assertThat(unit.getTurnoverStatus()).isEqualTo(Unit.TurnoverStatus.AWAITING_INSPECTION);
    }

    @Test
    void complete_setsStatusToCompleted_whenAllTasksArePassed() {
        Unit unit = new Unit(unitId, "Test unit", "Lagos", List.of("Bedroom", "Kitchen"), Unit.TurnoverStatus.OCCUPIED);
        PostCheckoutInspection inspection = PostCheckoutInspection.checkout(unit, "test-checkout");
        inspection.getTasks().forEach(inspection::checkTask);

        inspection.complete(unit);

        assertThat(inspection.getStatus()).isEqualTo(PostCheckoutInspection.Status.COMPLETED);
        assertThat(unit.getTurnoverStatus()).isEqualTo(Unit.TurnoverStatus.INSPECTED);
    }

    @Test
    void checkout_setsUnitTurnoverStatusToAwaitingInspection_whenUnitIsOccupied() {
        Unit unit = new Unit(unitId, "Test unit", "Lagos", List.of("Bedroom"), Unit.TurnoverStatus.OCCUPIED);

        PostCheckoutInspection.checkout(unit, "test-checkout");

        assertThat(unit.getTurnoverStatus()).isEqualTo(Unit.TurnoverStatus.AWAITING_INSPECTION);
    }
}
