package io.point3.p3api.order.application.state;

import io.point3.p3api.order.domain.entity.OrderConfirmation;
import java.util.UUID;

public interface OrderConfirmationStateUseCase {
  OrderConfirmation markBuyerViewed(UUID inquiryId, UUID confirmationId, UUID buyerUserId);

  OrderConfirmation requestRevision(UUID inquiryId, UUID confirmationId, UUID buyerUserId);

  OrderConfirmation replace(
      UUID inquiryId, UUID confirmationId, UUID replacementConfirmationId, UUID storeId);
}
