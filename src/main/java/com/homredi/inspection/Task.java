package com.homredi.inspection;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("tasks")
public class Task {
    @Id
    private Long id;
    private final String description;
    @Column("status")
    private State state;

    public enum State {
        PENDING,
        PASSED
    }

    private Task(Long id, String description, State state) {
        this.id = id;
        this.description = description;
        this.state = state;
    }

    public static Task createTask(String description) {
        return new Task(null, description, State.PENDING);
    }

    void check() {
        this.state = State.PASSED;
    }

    void uncheck() {
        if (state != State.PASSED) {
            throw new IllegalStateException("Only a passed task can be unchecked");
        }
        this.state = State.PENDING;
    }

}
