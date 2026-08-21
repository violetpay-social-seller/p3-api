package io.point3.p3api.order.application.state;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderConfirmationErrorCode;
import io.point3.p3api.order.application.query.OrderConfirmationQueryUseCase;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderConfirmationStateService implements OrderConfirmationStateUseCase {
  private final OrderConfirmationQueryUseCase orderConfirmationQueryUseCase;

  @Override
  public OrderConfirmation markBuyerViewed(
      UUID inquiryId, UUID confirmationId, UUID buyerUserId) {
    OrderConfirmation confirmation = orderConfirmationQueryUseCase.getBuyerConfirmation(
        inquiryId, confirmationId, buyerUserId);
    requireStatus(confirmation, OrderConfirmationStatus.SENT);
    confirmation.markBuyerViewed(Instant.now());
    return confirmation;
  }

  @Override
  public OrderConfirmation requestRevision(
      UUID inquiryId, UUID confirmationId, UUID buyerUserId) {
    OrderConfirmation confirmation = orderConfirmationQueryUseCase.getBuyerConfirmation(
        inquiryId, confirmationId, buyerUserId);
    requireStatus(confirmation, OrderConfirmationStatus.SENT);
    confirmation.requestRevision(Instant.now());
    return confirmation;
  }

  @Override
  public OrderConfirmation replace(
      UUID inquiryId, UUID confirmationId, UUID replacementConfirmationId, UUID storeId) {
    OrderConfirmation confirmation = orderConfirmationQueryUseCase.getSellerConfirmation(
        inquiryId, confirmationId, storeId);
    OrderConfirmation replacement = orderConfirmationQueryUseCase.getSellerConfirmation(
        inquiryId, replacementConfirmationId, storeId);
    requireStatus(confirmation, OrderConfirmationStatus.REVISION_REQUESTED);
    requireStatus(replacement, OrderConfirmationStatus.SENT);
    confirmation.replaceWith(replacement.getId());
    return confirmation;
  }

  private void requireStatus(
      OrderConfirmation confirmation, OrderConfirmationStatus requiredStatus) {
    if (confirmation.getStatus() != requiredStatus) {
      throw new BaseException(OrderConfirmationErrorCode.ORDER_CONFIRMATION_STATUS_FORBIDDEN);
    }
  }
}
