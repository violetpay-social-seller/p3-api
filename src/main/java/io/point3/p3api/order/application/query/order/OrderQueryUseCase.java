package io.point3.p3api.order.application.query.order;

import io.point3.p3api.order.application.result.OrderDetailResult;
import io.point3.p3api.order.application.result.OrderResult;
import java.util.List;
import java.util.UUID;

public interface OrderQueryUseCase {
  List<OrderResult> getBuyerOrders(UUID buyerUserId);

  OrderDetailResult getBuyerOrder(UUID orderId, UUID buyerUserId);

  List<OrderResult> getSellerOrders(UUID storeId);

  OrderDetailResult getSellerOrder(UUID orderId, UUID storeId);
}
