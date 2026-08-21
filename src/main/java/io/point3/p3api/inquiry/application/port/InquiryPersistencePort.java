package io.point3.p3api.inquiry.application.port;

import io.point3.p3api.inquiry.domain.entity.Inquiry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InquiryPersistencePort {

  Inquiry save(Inquiry inquiry);

  Optional<Inquiry> findById(UUID inquiryId);

  Optional<Inquiry> findByStoreIdAndBuyerUserId(UUID storeId, UUID userId);

  List<Inquiry> findAllByBuyerUserId(UUID buyerUserId);

  List<Inquiry> findAllByStoreId(UUID storeId);
}
