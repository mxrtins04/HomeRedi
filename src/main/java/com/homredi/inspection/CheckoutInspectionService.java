package com.homredi.inspection;

import com.homredi.unit.UnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class CheckoutInspectionService {

    private final PostCheckoutInspectionRepository inspections;
    private final UnitRepository units;

    public CheckoutInspectionService(PostCheckoutInspectionRepository inspections, UnitRepository units) {
        this.inspections = inspections;
        this.units = units;
    }

    @Transactional
    public PostCheckoutInspection startInspection(Long unitId, String checkoutEventId) {
        return inspections.findByUnitIdAndCheckoutEventId(unitId, checkoutEventId)
                .orElseGet(() -> {
                    var unit = units.findById(unitId)
                            .orElseThrow(() -> new NoSuchElementException("Unit not found: " + unitId));
                    PostCheckoutInspection inspection = PostCheckoutInspection.checkout(unit, checkoutEventId);
                    units.save(unit);
                    return inspections.save(inspection);
                });
    }
}
