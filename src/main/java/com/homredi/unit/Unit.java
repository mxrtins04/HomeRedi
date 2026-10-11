package com.homredi.unit;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Getter
@Table("units")
public final class Unit {

    public enum TurnoverStatus {
        OCCUPIED,
        AWAITING_INSPECTION,
        INSPECTED
    }

    @Id
    private Long id;
    private final String name;
    private final String location;
    private final List<String> spaces;
    private TurnoverStatus turnoverStatus;

    public Unit(Long id, String name, String location, List<String> spaces, TurnoverStatus turnoverStatus) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.spaces = List.copyOf(spaces);
        this.turnoverStatus = turnoverStatus;
    }

    public void awaitInspection() {
        this.turnoverStatus = TurnoverStatus.AWAITING_INSPECTION;
    }

    public void markInspected() {
        if (turnoverStatus != TurnoverStatus.AWAITING_INSPECTION) {
            throw new IllegalStateException("Only a unit awaiting inspection can be marked inspected");
        }
        this.turnoverStatus = TurnoverStatus.INSPECTED;
    }

}
