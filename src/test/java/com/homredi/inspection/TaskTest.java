package com.homredi.inspection;

import com.homredi.unit.Unit;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskTest {

    @Test
    void checkTask_setsTaskStateToPassed_whenTaskIsPending() {
        Unit unit = new Unit(1L, "Test unit", "Lagos", List.of("Bedroom"), Unit.TurnoverStatus.OCCUPIED);
        PostCheckoutInspection inspection = PostCheckoutInspection.checkout(unit, "test-checkout");
        Task task = inspection.getTasks().iterator().next();

        inspection.checkTask(task);

        assertThat(task.getState()).isEqualTo(Task.State.PASSED);
    }

    @Test
    void checkTask_throwsException_whenTaskDoesNotBelongToInspection() {
        Unit firstUnit = new Unit(1L, "Test unit", "Lagos", List.of("Bedroom"), Unit.TurnoverStatus.OCCUPIED);
        Unit secondUnit = new Unit(2L, "Test unit", "Lagos", List.of("Kitchen"), Unit.TurnoverStatus.OCCUPIED);
        PostCheckoutInspection inspection = PostCheckoutInspection.checkout(firstUnit, "first-checkout");
        Task taskFromAnotherInspection = PostCheckoutInspection.checkout(secondUnit, "second-checkout")
                .getTasks().iterator().next();

        assertThatThrownBy(() -> inspection.checkTask(taskFromAnotherInspection))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void checkTask_throwsException_whenInspectionIsCompleted() {
        Unit unit = new Unit(1L, "Test unit", "Lagos", List.of("Bedroom"), Unit.TurnoverStatus.OCCUPIED);
        PostCheckoutInspection inspection = PostCheckoutInspection.checkout(unit, "test-checkout");
        Task task = inspection.getTasks().iterator().next();
        inspection.checkTask(task);
        inspection.complete(unit);

        assertThatThrownBy(() -> inspection.checkTask(task))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void uncheckTask_setsTaskStateToPending_whenTaskIsPassedDuringPendingInspection() {
        Unit unit = new Unit(1L, "Test unit", "Lagos", List.of("Bedroom"), Unit.TurnoverStatus.OCCUPIED);
        PostCheckoutInspection inspection = PostCheckoutInspection.checkout(unit, "test-checkout");
        Task task = inspection.getTasks().iterator().next();
        inspection.checkTask(task);

        inspection.uncheckTask(task);

        assertThat(task.getState()).isEqualTo(Task.State.PENDING);
    }

    @Test
    void uncheckTask_throwsException_whenTaskIsAlreadyPending() {
        Unit unit = new Unit(1L, "Test unit", "Lagos", List.of("Bedroom"), Unit.TurnoverStatus.OCCUPIED);
        PostCheckoutInspection inspection = PostCheckoutInspection.checkout(unit, "test-checkout");
        Task task = inspection.getTasks().iterator().next();

        assertThatThrownBy(() -> inspection.uncheckTask(task))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void uncheckTask_keepsTaskPassed_whenInspectionIsCompleted() {
        Unit unit = new Unit(1L, "Test unit", "Lagos", List.of("Bedroom"), Unit.TurnoverStatus.OCCUPIED);
        PostCheckoutInspection inspection = PostCheckoutInspection.checkout(unit, "test-checkout");
        Task task = inspection.getTasks().iterator().next();
        inspection.checkTask(task);
        inspection.complete(unit);

        assertThatThrownBy(() -> inspection.uncheckTask(task))
                .isInstanceOf(IllegalStateException.class);
        assertThat(task.getState()).isEqualTo(Task.State.PASSED);
    }
}
