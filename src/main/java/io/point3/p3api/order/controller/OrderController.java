package io.point3.p3api.order.controller;

import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.order.application.calendar.OrderCalendarQueryUseCase;
import io.point3.p3api.order.application.query.order.OrderQueryUseCase;
import io.point3.p3api.order.application.state.CompleteOrderPickupCommand;
import io.point3.p3api.order.application.state.OrderStateUseCase;
import io.point3.p3api.order.application.state.RefundOrderCommand;
import io.point3.p3api.order.application.state.RequestOrderCancelCommand;
import io.point3.p3api.order.controller.request.OrderCancelRequest;
import io.point3.p3api.order.controller.response.OrderCalendarResponse;
import io.point3.p3api.order.controller.response.OrderDetailResponse;
import io.point3.p3api.order.controller.response.OrderResponse;
import io.point3.p3api.order.domain.type.OrderStatus;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {
  private final OrderQueryUseCase orderQueryUseCase;
  private final OrderStateUseCase orderStateUseCase;
  private final OrderCalendarQueryUseCase orderCalendarQueryUseCase;

  @GetMapping("/orders")
  public ApiResponse<List<OrderResponse>> getBuyerOrders(@Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);
    return ApiResponse.ok(orderQueryUseCase.getBuyerOrders(currentUser.userId()).stream()
        .map(OrderResponse::from)
        .toList());
  }

  @GetMapping("/orders/{orderId}")
  public ApiResponse<OrderDetailResponse> getBuyerOrder(
      @PathVariable UUID orderId, @Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);
    return ApiResponse.ok(
        OrderDetailResponse.from(orderQueryUseCase.getBuyerOrder(orderId, currentUser.userId())));
  }

  @PostMapping("/orders/{orderId}/cancel-request")
  public ApiResponse<OrderResponse> requestCancel(
      @PathVariable UUID orderId,
      @Authenticated CurrentUser currentUser,
      @Valid @RequestBody OrderCancelRequest request) {
    RoleGuard.requireBuyer(currentUser);
    return ApiResponse.ok(OrderResponse.from(orderStateUseCase.requestCancel(
        RequestOrderCancelCommand.of(orderId, currentUser.userId(), request.reason()))));
  }

  @GetMapping("/seller/orders")
  public ApiResponse<List<OrderResponse>> getSellerOrders(@CurrentStoreId UUID storeId) {
    return ApiResponse.ok(orderQueryUseCase.getSellerOrders(storeId).stream()
        .map(OrderResponse::from)
        .toList());
  }

  @GetMapping("/seller/orders/calendar/today")
  public ApiResponse<OrderCalendarResponse> getTodayOrders(
      @CurrentStoreId UUID storeId, @RequestParam(required = false) OrderStatus status) {
    return ApiResponse.ok(
        OrderCalendarResponse.from(orderCalendarQueryUseCase.getToday(storeId, status)));
  }

  @GetMapping("/seller/orders/calendar/week")
  public ApiResponse<OrderCalendarResponse> getWeekOrders(
      @CurrentStoreId UUID storeId, @RequestParam(required = false) OrderStatus status) {
    return ApiResponse.ok(
        OrderCalendarResponse.from(orderCalendarQueryUseCase.getThisWeek(storeId, status)));
  }

  @GetMapping("/seller/orders/calendar/month")
  public ApiResponse<OrderCalendarResponse> getMonthOrders(
      @CurrentStoreId UUID storeId,
      @RequestParam int year,
      @RequestParam int month,
      @RequestParam(required = false) OrderStatus status) {
    return ApiResponse.ok(OrderCalendarResponse.from(
        orderCalendarQueryUseCase.getMonth(storeId, year, month, status)));
  }

  @GetMapping("/seller/orders/{orderId}")
  public ApiResponse<OrderDetailResponse> getSellerOrder(
      @PathVariable UUID orderId, @CurrentStoreId UUID storeId) {
    return ApiResponse.ok(
        OrderDetailResponse.from(orderQueryUseCase.getSellerOrder(orderId, storeId)));
  }

  @PatchMapping("/seller/orders/{orderId}/pickup")
  public ApiResponse<OrderResponse> pickUp(
      @PathVariable UUID orderId, @CurrentStoreId UUID storeId) {
    return ApiResponse.ok(OrderResponse.from(
        orderStateUseCase.pickUp(CompleteOrderPickupCommand.of(orderId, storeId))));
  }

  @PostMapping("/seller/orders/{orderId}/refund")
  public ApiResponse<OrderDetailResponse> refund(
      @PathVariable UUID orderId,
      @CurrentStoreId UUID storeId,
      @Authenticated CurrentUser currentUser,
      @Valid @RequestBody OrderCancelRequest request) {
    RoleGuard.requireSeller(currentUser);
    return ApiResponse.ok(OrderDetailResponse.from(orderStateUseCase.refund(
        RefundOrderCommand.of(orderId, storeId, currentUser.userId(), request.reason()))));
  }
}
