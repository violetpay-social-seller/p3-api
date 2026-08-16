package io.point3.p3api.order.controller;

import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.order.application.result.SendOrderConfirmationResult;
import io.point3.p3api.order.application.send.SendOrderConfirmationCommand;
import io.point3.p3api.order.application.send.SendOrderConfirmationUseCase;
import io.point3.p3api.order.controller.request.OrderConfirmationSendRequest;
import io.point3.p3api.order.controller.response.OrderConfirmationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/seller/inquiries/{inquiryId}/confirmations")
@RequiredArgsConstructor
public class SellerOrderConfirmationController {

    private final SendOrderConfirmationUseCase sendOrderConfirmationUseCase;

    @PostMapping
    public ApiResponse<OrderConfirmationResponse> send(
            @PathVariable UUID inquiryId,
            @Authenticated CurrentUser currentUser,
            @CurrentStoreId UUID storeId,
            @Valid @RequestBody OrderConfirmationSendRequest request
    ) {
        SendOrderConfirmationResult result = sendOrderConfirmationUseCase.send(
                toCommand(inquiryId, storeId, currentUser.userId(), request)
        );

        return ApiResponse.ok(OrderConfirmationResponse.from(result));
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
