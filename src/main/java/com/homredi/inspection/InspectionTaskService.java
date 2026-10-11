package com.homredi.inspection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class InspectionTaskService {

    private final PostCheckoutInspectionRepository inspections;

    public InspectionTaskService(PostCheckoutInspectionRepository inspections) {
        this.inspections = inspections;
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
