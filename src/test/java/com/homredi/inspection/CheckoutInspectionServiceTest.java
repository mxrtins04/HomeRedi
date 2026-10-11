package com.homredi.inspection;

import com.homredi.unit.Unit;
import com.homredi.unit.UnitRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckoutInspectionServiceTest {

    @Test
    void startInspection_createsAndSavesInspection_whenCheckoutEventIsNew() {
        Long unitId = 2L;
        String checkoutEventId = "checkout-456";
        Unit unit = new Unit(unitId, "Test unit", "Lagos", List.of("Bedroom", "Kitchen"), Unit.TurnoverStatus.OCCUPIED);
        PostCheckoutInspectionRepository inspections = mock(PostCheckoutInspectionRepository.class);
        UnitRepository units = mock(UnitRepository.class);
        when(inspections.findByUnitIdAndCheckoutEventId(unitId, checkoutEventId)).thenReturn(Optional.empty());
        when(units.findById(unitId)).thenReturn(Optional.of(unit));
        when(inspections.save(any(PostCheckoutInspection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CheckoutInspectionService service = new CheckoutInspectionService(inspections, units);

        PostCheckoutInspection result = service.startInspection(unitId, checkoutEventId);

        assertThat(result.getUnitId()).isEqualTo(unit.getId());
        assertThat(result.getCheckoutEventId()).isEqualTo(checkoutEventId);
        assertThat(result.getStatus()).isEqualTo(PostCheckoutInspection.Status.PENDING);
        assertThat(result.getTasks()).hasSize(2)
                .extracting(Task::getState)
                .containsOnly(Task.State.PENDING);
        assertThat(result.getTasks()).extracting(Task::getDescription)
                .containsExactlyInAnyOrder("Bedroom task", "Kitchen task");
        assertThat(unit.getTurnoverStatus()).isEqualTo(Unit.TurnoverStatus.AWAITING_INSPECTION);
        verify(inspections).findByUnitIdAndCheckoutEventId(unitId, checkoutEventId);
        verify(units).findById(unitId);
        verify(inspections).save(same(result));
    }

    @Test
    void startInspection_returnsExistingInspection_whenCheckoutEventIsRetried() {
        Long unitId = 1L;
        String checkoutEventId = "checkout-123";
        PostCheckoutInspection existingInspection = PostCheckoutInspection.checkout(
                new Unit(unitId, "Test unit", "Lagos", List.of("Bedroom"), Unit.TurnoverStatus.OCCUPIED), checkoutEventId);
        PostCheckoutInspectionRepository inspections = mock(PostCheckoutInspectionRepository.class);
        UnitRepository units = mock(UnitRepository.class);
        when(inspections.findByUnitIdAndCheckoutEventId(unitId, checkoutEventId))
                .thenReturn(Optional.of(existingInspection));
        CheckoutInspectionService service = new CheckoutInspectionService(inspections, units);

        PostCheckoutInspection result = service.startInspection(unitId, checkoutEventId);

        assertThat(result).isSameAs(existingInspection);
        verify(inspections).findByUnitIdAndCheckoutEventId(unitId, checkoutEventId);
        verify(units, never()).findById(anyLong());
        verify(inspections, never()).save(any(PostCheckoutInspection.class));
    }
}
