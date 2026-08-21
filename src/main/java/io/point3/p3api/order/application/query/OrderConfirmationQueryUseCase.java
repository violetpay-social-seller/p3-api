package io.point3.p3api.order.application.query;

import io.point3.p3api.order.domain.entity.OrderConfirmation;
import java.util.List;
import java.util.UUID;

public interface OrderConfirmationQueryUseCase {

  OrderConfirmation getSellerConfirmation(UUID inquiryId, UUID confirmationId, UUID storeId);

  OrderConfirmation getBuyerConfirmation(UUID inquiryId, UUID confirmationId, UUID buyerUserId);

  List<OrderConfirmation> getSellerHistory(UUID inquiryId, UUID storeId);

  List<OrderConfirmation> getBuyerHistory(UUID inquiryId, UUID buyerUserId);
}
