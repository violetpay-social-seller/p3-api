package io.point3.p3api.order.controller;

import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.order.application.query.OrderConfirmationQueryUseCase;
import io.point3.p3api.order.application.result.SendOrderConfirmationResult;
import io.point3.p3api.order.application.send.SendOrderConfirmationCommand;
import io.point3.p3api.order.application.send.SendOrderConfirmationUseCase;
import io.point3.p3api.order.application.state.OrderConfirmationStateUseCase;
import io.point3.p3api.order.controller.request.OrderConfirmationReplaceRequest;
import io.point3.p3api.order.controller.request.OrderConfirmationSendRequest;
import io.point3.p3api.order.controller.response.OrderConfirmationDetailResponse;
import io.point3.p3api.order.controller.response.OrderConfirmationResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/inquiries/{inquiryId}/confirmations")
@RequiredArgsConstructor
public class SellerOrderConfirmationController {

  private final SendOrderConfirmationUseCase sendOrderConfirmationUseCase;
  private final OrderConfirmationQueryUseCase orderConfirmationQueryUseCase;
  private final OrderConfirmationStateUseCase orderConfirmationStateUseCase;

  @GetMapping
  public ApiResponse<List<OrderConfirmationDetailResponse>> getHistory(
      @PathVariable UUID inquiryId, @CurrentStoreId UUID storeId) {
    return ApiResponse.ok(
        orderConfirmationQueryUseCase.getSellerHistory(inquiryId, storeId).stream()
            .map(OrderConfirmationDetailResponse::from)
            .toList());
  }

  @PostMapping
  public ApiResponse<OrderConfirmationResponse> send(
      @PathVariable UUID inquiryId,
      @Authenticated CurrentUser currentUser,
      @CurrentStoreId UUID storeId,
      @Valid @RequestBody OrderConfirmationSendRequest request) {
    SendOrderConfirmationResult result = sendOrderConfirmationUseCase.send(
        toCommand(inquiryId, storeId, currentUser.userId(), request));

    return ApiResponse.ok(OrderConfirmationResponse.from(result));
  }

  @GetMapping("/{confirmationId}")
  public ApiResponse<OrderConfirmationDetailResponse> getDetail(
      @PathVariable UUID inquiryId,
      @PathVariable UUID confirmationId,
      @Authenticated CurrentUser currentUser,
      @CurrentStoreId UUID storeId) {
    return ApiResponse.ok(OrderConfirmationDetailResponse.from(
        orderConfirmationQueryUseCase.getSellerConfirmation(inquiryId, confirmationId, storeId)));
  }

  @PatchMapping("/{confirmationId}/replacement")
  public ApiResponse<OrderConfirmationDetailResponse> replace(
      @PathVariable UUID inquiryId,
      @PathVariable UUID confirmationId,
      @CurrentStoreId UUID storeId,
      @Valid @RequestBody OrderConfirmationReplaceRequest request) {
    return ApiResponse.ok(
        OrderConfirmationDetailResponse.from(orderConfirmationStateUseCase.replace(
            inquiryId, confirmationId, request.replacementConfirmationId(), storeId)));
  }

  private SendOrderConfirmationCommand toCommand(
      UUID inquiryId, UUID storeId, UUID sellerUserId, OrderConfirmationSendRequest request) {
    return new SendOrderConfirmationCommand(
        inquiryId,
        storeId,
        sellerUserId,
        request.orderFormSubmissionId(),
        request.confirmationTitle(),
        request.summaryText(),
        request.amount(),
        request.pickupAt(),
        request.additionalItems().stream()
            .map(item -> new SendOrderConfirmationCommand.AdditionalItem(
                item.label(), item.value(), item.amount()))
            .toList(),
        request.sellerNote());
  }
}
