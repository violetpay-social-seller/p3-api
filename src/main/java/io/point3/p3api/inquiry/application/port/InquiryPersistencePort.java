package io.point3.p3api.inquiry.application.port;

import io.point3.p3api.inquiry.domain.entity.Inquiry;

import java.util.Optional;
import java.util.UUID;

public interface InquiryPersistencePort {

    Inquiry save(Inquiry inquiry);

    Optional<Inquiry> findByStoreIdAndBuyerUserId(UUID storeId, UUID userId);
}
