package io.point3.p3api.inquiry.infrastructure.persistence;

import io.point3.p3api.inquiry.domain.entity.Inquiry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryJpaRepository extends JpaRepository<Inquiry, UUID> {
  Optional<Inquiry> findByStoreIdAndBuyerUserId(UUID storeId, UUID buyerUserId);

  List<Inquiry> findAllByBuyerUserIdOrderByCreatedAtDesc(UUID buyerUserId);

  List<Inquiry> findAllByStoreIdOrderByCreatedAtDesc(UUID storeId);
}
