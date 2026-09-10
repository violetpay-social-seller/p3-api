package io.point3.p3api.order.infrastructure.persistence;

import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderConfirmationJpaRepository extends JpaRepository<OrderConfirmation, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select orderConfirmation from OrderConfirmation orderConfirmation where orderConfirmation.id = :id")
  Optional<OrderConfirmation> findByIdForUpdate(@Param("id") UUID id);

  List<OrderConfirmation> findAllByInquiryIdOrderByCreatedAtDesc(UUID inquiryId);

  Optional<OrderConfirmation> findFirstByInquiryIdAndStatusOrderByCreatedAtDesc(
      UUID inquiryId, OrderConfirmationStatus status);
}
