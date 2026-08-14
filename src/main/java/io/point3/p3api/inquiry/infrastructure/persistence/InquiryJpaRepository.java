package io.point3.p3api.inquiry.infrastructure.persistence;

import io.point3.p3api.inquiry.domain.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InquiryJpaRepository extends JpaRepository<Inquiry, UUID> {
    Optional<Inquiry> findByStoreIdAndBuyerUserId(UUID storeId, UUID userId);
}
