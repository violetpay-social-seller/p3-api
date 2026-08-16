package io.point3.p3api.order.infrastructure.persistence;

import io.point3.p3api.order.application.port.OrderConfirmationPersistencePort;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class OrderConfirmationPersistenceAdapter implements OrderConfirmationPersistencePort {

  private final OrderConfirmationJpaRepository orderConfirmationJpaRepository;

  @Override
  public OrderConfirmation save(OrderConfirmation orderConfirmation) {
    return orderConfirmationJpaRepository.save(orderConfirmation);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<OrderConfirmation> findById(UUID orderConfirmationId) {
    return orderConfirmationJpaRepository.findById(orderConfirmationId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderConfirmation> findAllByInquiryId(UUID inquiryId) {
    return orderConfirmationJpaRepository.findAllByInquiryIdOrderByCreatedAtDesc(inquiryId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<OrderConfirmation> findLatestByInquiryIdAndStatus(
      UUID inquiryId, OrderConfirmationStatus status) {
    return orderConfirmationJpaRepository.findFirstByInquiryIdAndStatusOrderByCreatedAtDesc(
        inquiryId, status);
  }
}
