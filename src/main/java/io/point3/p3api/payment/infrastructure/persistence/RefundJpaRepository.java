package io.point3.p3api.payment.infrastructure.persistence;

import io.point3.p3api.payment.domain.entity.Refund;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundJpaRepository extends JpaRepository<Refund, UUID> {
  List<Refund> findAllByOrderIdOrderByCreatedAtDesc(UUID orderId);
}
