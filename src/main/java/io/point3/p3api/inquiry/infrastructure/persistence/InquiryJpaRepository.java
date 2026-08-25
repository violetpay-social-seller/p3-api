package io.point3.p3api.inquiry.infrastructure.persistence;

import io.point3.p3api.inquiry.domain.entity.Inquiry;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InquiryJpaRepository extends JpaRepository<Inquiry, UUID> {
  Optional<Inquiry> findByStoreIdAndBuyerUserId(UUID storeId, UUID buyerUserId);

  List<Inquiry> findAllByBuyerUserIdOrderByCreatedAtDesc(UUID buyerUserId);

  List<Inquiry> findAllByStoreIdOrderByCreatedAtDesc(UUID storeId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      update Inquiry inquiry
      set inquiry.buyerPurgedAt = :purgedAt
      where inquiry.buyerDeletedAt is not null
        and inquiry.buyerPurgedAt is null
        and inquiry.buyerDeletedAt <= :cutoff
      """)
  int purgeExpiredBuyerTrash(@Param("cutoff") Instant cutoff, @Param("purgedAt") Instant purgedAt);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      update Inquiry inquiry
      set inquiry.sellerPurgedAt = :purgedAt
      where inquiry.sellerDeletedAt is not null
        and inquiry.sellerPurgedAt is null
        and inquiry.sellerDeletedAt <= :cutoff
      """)
  int purgeExpiredSellerTrash(@Param("cutoff") Instant cutoff, @Param("purgedAt") Instant purgedAt);
}
