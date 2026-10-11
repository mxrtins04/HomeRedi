package com.homredi.inspection;

import lombok.Getter;
import com.homredi.unit.Unit;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Table("post_checkout_inspections")
public final class PostCheckoutInspection {

    public enum Status {
        PENDING,
        COMPLETED
    }

    @Id
    private Long id;
    private final Long unitId;
    private final String checkoutEventId;
    private Status status;
    @MappedCollection(idColumn = "post_checkout_inspection_id")
    private final Set<Task> tasks;

    @PersistenceCreator
    private PostCheckoutInspection(Long id, Long unitId, String checkoutEventId, Status status, Set<Task> tasks) {
        this.id = id;
        this.unitId = unitId;
        this.checkoutEventId = checkoutEventId;
        this.status = status;
        this.tasks = Set.copyOf(tasks);
    }

    public static PostCheckoutInspection checkout(Unit unit, String checkoutEventId) {
        unit.awaitInspection();
        List<String> spaces = unit.getSpaces();
        Set<Task> tasks = generateTasks(spaces);
        return new PostCheckoutInspection(null, unit.getId(), checkoutEventId, Status.PENDING, tasks);
    }

    public void checkTask(Task task) {
        if (status != Status.PENDING) {
            throw new IllegalStateException("Tasks cannot be checked after inspection completion");
        }
        if (!tasks.contains(task)) {
            throw new IllegalArgumentException("Task does not belong to this inspection");
        }
        task.check();
    }

    public void uncheckTask(Task task) {
        if (status != Status.PENDING) {
            throw new IllegalStateException("Tasks cannot be unchecked after inspection completion");
        }
        if (!tasks.contains(task)) {
            throw new IllegalArgumentException("Task does not belong to this inspection");
        }
        task.uncheck();
    }

    public void complete(Unit unit) {
        if (status != Status.PENDING) {
            throw new IllegalStateException("Only a pending inspection can be completed");
        }
        if (!unit.getId().equals(unitId)) {
            throw new IllegalArgumentException("Unit does not belong to this inspection");
        }
        if (tasks.stream().anyMatch(task -> task.getState() != Task.State.PASSED)) {
            throw new IllegalStateException("Every task must be passed before completing the inspection");
        }

        unit.markInspected();
        status = Status.COMPLETED;
    }

    private static Set<Task> generateTasks(List<String> spaces) {
        return spaces.stream()
                .map(space -> Task.createTask(space + " task"))
                .collect(Collectors.toSet());
    }

}
