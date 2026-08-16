package io.point3.p3api.order.infrastructure.persistence;

import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderConfirmationJpaRepository extends JpaRepository<OrderConfirmation, UUID> {

  List<OrderConfirmation> findAllByInquiryIdOrderByCreatedAtDesc(UUID inquiryId);

  Optional<OrderConfirmation> findFirstByInquiryIdAndStatusOrderByCreatedAtDesc(
      UUID inquiryId, OrderConfirmationStatus status);
}
