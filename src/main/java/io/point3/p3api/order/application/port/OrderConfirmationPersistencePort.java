package io.point3.p3api.order.application.port;

import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderConfirmationPersistencePort {

  OrderConfirmation save(OrderConfirmation orderConfirmation);

  Optional<OrderConfirmation> findById(UUID orderConfirmationId);

  List<OrderConfirmation> findAllByInquiryId(UUID inquiryId);

  Optional<OrderConfirmation> findLatestByInquiryIdAndStatus(
      UUID inquiryId, OrderConfirmationStatus status);
}
