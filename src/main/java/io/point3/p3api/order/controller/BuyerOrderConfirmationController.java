package io.point3.p3api.order.controller;

import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.order.application.query.OrderConfirmationQueryUseCase;
import io.point3.p3api.order.application.state.OrderConfirmationStateUseCase;
import io.point3.p3api.order.controller.response.OrderConfirmationDetailResponse;
import java.util.UUID;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inquiries/{inquiryId}/confirmations")
@RequiredArgsConstructor
public class BuyerOrderConfirmationController {

  private final OrderConfirmationQueryUseCase orderConfirmationQueryUseCase;
  private final OrderConfirmationStateUseCase orderConfirmationStateUseCase;

  @GetMapping
  public ApiResponse<List<OrderConfirmationDetailResponse>> getHistory(
      @PathVariable UUID inquiryId, @Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);
    return ApiResponse.ok(
        orderConfirmationQueryUseCase.getBuyerHistory(inquiryId, currentUser.userId()).stream()
            .map(OrderConfirmationDetailResponse::from)
            .toList());
  }

  @GetMapping("/{confirmationId}")
  public ApiResponse<OrderConfirmationDetailResponse> getDetail(
      @PathVariable UUID inquiryId,
      @PathVariable UUID confirmationId,
      @Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);

    return ApiResponse.ok(
        OrderConfirmationDetailResponse.from(orderConfirmationQueryUseCase.getBuyerConfirmation(
            inquiryId, confirmationId, currentUser.userId())));
  }

  @PatchMapping("/{confirmationId}/viewed")
  public ApiResponse<OrderConfirmationDetailResponse> markViewed(
      @PathVariable UUID inquiryId,
      @PathVariable UUID confirmationId,
      @Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);
    return ApiResponse.ok(OrderConfirmationDetailResponse.from(
        orderConfirmationStateUseCase.markBuyerViewed(
            inquiryId, confirmationId, currentUser.userId())));
  }

  @PatchMapping("/{confirmationId}/revision")
  public ApiResponse<OrderConfirmationDetailResponse> requestRevision(
      @PathVariable UUID inquiryId,
      @PathVariable UUID confirmationId,
      @Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);
    return ApiResponse.ok(OrderConfirmationDetailResponse.from(
        orderConfirmationStateUseCase.requestRevision(
            inquiryId, confirmationId, currentUser.userId())));
  }
}
