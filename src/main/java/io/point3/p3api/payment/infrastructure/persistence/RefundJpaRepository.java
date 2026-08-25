package io.point3.p3api.payment.infrastructure.persistence;

import io.point3.p3api.payment.domain.entity.Refund;
import io.point3.p3api.payment.domain.type.RefundStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundJpaRepository extends JpaRepository<Refund, UUID> {
  List<Refund> findAllByOrderIdOrderByCreatedAtDesc(UUID orderId);

  @Query("""
      select coalesce(sum(r.amount), 0)
      from Refund r, Order o
      where r.orderId = o.id
        and o.storeId = :storeId
        and r.status = :status
        and r.completedAt >= :startInclusive
        and r.completedAt < :endExclusive
      """)
  long sumCompletedAmount(
      @Param("storeId") UUID storeId,
      @Param("status") RefundStatus status,
      @Param("startInclusive") Instant startInclusive,
      @Param("endExclusive") Instant endExclusive);
}
