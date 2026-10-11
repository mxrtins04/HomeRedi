package com.homredi.inspection;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface PostCheckoutInspectionRepository extends CrudRepository<PostCheckoutInspection, Long> {

    Optional<PostCheckoutInspection> findByUnitIdAndCheckoutEventId(Long unitId, String checkoutEventId);
}
