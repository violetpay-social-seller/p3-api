package io.point3.p3api.order.application.query;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderConfirmationErrorCode;
import io.point3.p3api.inquiry.application.chat.InquiryChatAccessService;
import io.point3.p3api.order.application.port.OrderConfirmationPersistencePort;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderConfirmationQueryService implements OrderConfirmationQueryUseCase {

  private final InquiryChatAccessService inquiryChatAccessService;
  private final OrderConfirmationPersistencePort orderConfirmationPersistencePort;

  @Override
  public OrderConfirmation getSellerConfirmation(
      UUID inquiryId, UUID confirmationId, UUID storeId) {
    inquiryChatAccessService.getSellerInquiry(inquiryId, storeId);
    return getConfirmation(inquiryId, confirmationId);
  }

  @Override
  public OrderConfirmation getBuyerConfirmation(
      UUID inquiryId, UUID confirmationId, UUID buyerUserId) {
    inquiryChatAccessService.getBuyerInquiry(inquiryId, buyerUserId);
    return getConfirmation(inquiryId, confirmationId);
  }

  private OrderConfirmation getConfirmation(UUID inquiryId, UUID confirmationId) {
    OrderConfirmation confirmation = orderConfirmationPersistencePort
        .findById(confirmationId)
        .orElseThrow(
            () -> new BaseException(OrderConfirmationErrorCode.ORDER_CONFIRMATION_NOT_FOUND));

    if (!confirmation.getInquiryId().equals(inquiryId)) {
      throw new BaseException(OrderConfirmationErrorCode.ORDER_CONFIRMATION_NOT_FOUND);
    }

    return confirmation;
  }
}
