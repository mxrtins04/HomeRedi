package com.homredi.inspection;

import com.homredi.unit.UnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class InspectionTaskService {

    private final PostCheckoutInspectionRepository inspections;
    private final UnitRepository units;

    public InspectionTaskService(PostCheckoutInspectionRepository inspections, UnitRepository units) {
        this.inspections = inspections;
        this.units = units;
    }

    @Transactional
    public void checkTask(Long inspectionId, Long taskId) {
        PostCheckoutInspection inspection = loadInspection(inspectionId);
        Task task = findTask(inspection, taskId);

        inspection.checkTask(task);
        inspections.save(inspection);
    }

    @Transactional
    public void uncheckTask(Long inspectionId, Long taskId) {
        PostCheckoutInspection inspection = loadInspection(inspectionId);
        Task task = findTask(inspection, taskId);

        inspection.uncheckTask(task);
        inspections.save(inspection);
    }

    @Transactional
    public void completeInspection(Long inspectionId) {
        PostCheckoutInspection inspection = loadInspection(inspectionId);
        var unit = units.findById(inspection.getUnitId())
                .orElseThrow(() -> new NoSuchElementException("Unit not found: " + inspection.getUnitId()));

        inspection.complete(unit);
        units.save(unit);
        inspections.save(inspection);
    }

    private PostCheckoutInspection loadInspection(Long inspectionId) {
        return inspections.findById(inspectionId)
                .orElseThrow(() -> new NoSuchElementException("Inspection not found: " + inspectionId));
    }

    private Task findTask(PostCheckoutInspection inspection, Long taskId) {
        return inspection.getTasks().stream()
                .filter(candidate -> candidate.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "Task not found in inspection " + inspection.getId() + ": " + taskId));
    }
}
