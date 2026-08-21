package io.point3.p3api.order.controller;

import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.order.application.query.order.OrderQueryUseCase;
import io.point3.p3api.order.controller.response.OrderResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {
  private final OrderQueryUseCase orderQueryUseCase;

  @GetMapping("/orders")
  public ApiResponse<List<OrderResponse>> getBuyerOrders(@Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);
    return ApiResponse.ok(orderQueryUseCase.getBuyerOrders(currentUser.userId()).stream()
        .map(OrderResponse::from)
        .toList());
  }

  @GetMapping("/orders/{orderId}")
  public ApiResponse<OrderResponse> getBuyerOrder(
      @PathVariable UUID orderId, @Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);
    return ApiResponse.ok(
        OrderResponse.from(orderQueryUseCase.getBuyerOrder(orderId, currentUser.userId())));
  }

  @GetMapping("/seller/orders")
  public ApiResponse<List<OrderResponse>> getSellerOrders(@CurrentStoreId UUID storeId) {
    return ApiResponse.ok(orderQueryUseCase.getSellerOrders(storeId).stream()
        .map(OrderResponse::from)
        .toList());
  }

  @GetMapping("/seller/orders/{orderId}")
  public ApiResponse<OrderResponse> getSellerOrder(
      @PathVariable UUID orderId, @CurrentStoreId UUID storeId) {
    return ApiResponse.ok(OrderResponse.from(orderQueryUseCase.getSellerOrder(orderId, storeId)));
  }
}
