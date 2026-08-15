package io.point3.p3api.inquiry.controller;

import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.inquiry.application.command.SubmitPreOrderCommand;
import io.point3.p3api.inquiry.application.result.SubmitPreOrderResult;
import io.point3.p3api.inquiry.application.submit.SubmitPreOrderUseCase;
import io.point3.p3api.inquiry.controller.request.SubmitPreOrderRequest;
import io.point3.p3api.inquiry.controller.response.SubmitPreOrderResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stores/{slug}/pre-orders")
@RequiredArgsConstructor
public class PreOrderController {

  private final SubmitPreOrderUseCase submitPreOrderUseCase;

  @PostMapping
  public ApiResponse<SubmitPreOrderResponse> submit(
      @Authenticated CurrentUser currentUser,
      @CurrentStoreId UUID storeId,
      @Valid @RequestBody SubmitPreOrderRequest request) {
    RoleGuard.requireBuyer(currentUser);

    SubmitPreOrderResult result =
        submitPreOrderUseCase.submit(toCommand(storeId, currentUser, request));

    return ApiResponse.ok(SubmitPreOrderResponse.from(result));
  }

  private SubmitPreOrderCommand toCommand(
      UUID storeId, CurrentUser currentUser, SubmitPreOrderRequest request) {
    return SubmitPreOrderCommand.of(
        storeId,
        currentUser.userId(),
        request.productId(),
        request.clientRequestId(),
        request.orderFormTemplateId(),
        request.formAnswers().stream()
            .map(answer -> new SubmitPreOrderCommand.FormAnswer(answer.fieldId(), answer.value()))
            .toList(),
        request.productOptionSelections().stream()
            .map(selection -> new SubmitPreOrderCommand.ProductOptionSelection(
                selection.optionGroupId(), selection.optionIds()))
            .toList());
  }
}
