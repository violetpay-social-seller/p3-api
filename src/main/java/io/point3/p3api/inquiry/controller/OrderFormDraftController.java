package io.point3.p3api.inquiry.controller;

import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.inquiry.application.command.ConsumeOrderFormDraftCommand;
import io.point3.p3api.inquiry.application.command.CreateOrderFormDraftCommand;
import io.point3.p3api.inquiry.application.draft.consume.OrderFormDraftConsumeUseCase;
import io.point3.p3api.inquiry.application.draft.create.OrderFormDraftCreateUseCase;
import io.point3.p3api.inquiry.application.result.OrderFormDraftConsumeResult;
import io.point3.p3api.inquiry.application.result.OrderFormDraftResult;
import io.point3.p3api.inquiry.controller.request.CreateOrderFormDraftRequest;
import io.point3.p3api.inquiry.controller.response.OrderFormDraftConsumeResponse;
import io.point3.p3api.inquiry.controller.response.OrderFormDraftResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * draft 생성 API 와 consume API 입구
 */
@RestController
@RequiredArgsConstructor
public class OrderFormDraftController {

  private final OrderFormDraftCreateUseCase orderFormDraftCreateUseCase;
  private final OrderFormDraftConsumeUseCase orderFormDraftConsumeUseCase;

  @PostMapping("/stores/{slug}/order-form-drafts")
  public ApiResponse<OrderFormDraftResponse> create(
      @PathVariable String slug,
      @CurrentStoreId UUID storeId,
      @Valid @RequestBody CreateOrderFormDraftRequest request) {
    OrderFormDraftResult result = orderFormDraftCreateUseCase.create(toCommand(storeId, request));
    return ApiResponse.ok(OrderFormDraftResponse.from(result));
  }

  @PostMapping("/order-form-drafts/{draftKey}/consume")
  public ApiResponse<OrderFormDraftConsumeResponse> consume(
      @PathVariable String draftKey, @Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);

    OrderFormDraftConsumeResult result = orderFormDraftConsumeUseCase.consume(
        new ConsumeOrderFormDraftCommand(draftKey, currentUser.userId()));
    return ApiResponse.ok(OrderFormDraftConsumeResponse.from(result));
  }

  private CreateOrderFormDraftCommand toCommand(UUID storeId, CreateOrderFormDraftRequest request) {
    return new CreateOrderFormDraftCommand(
        storeId,
        request.orderFormTemplateId(),
        request.pickupDate(),
        request.pickupTime(),
        request.noticeAgreed(),
        request.formAnswers().stream()
            .map(answer ->
                new CreateOrderFormDraftCommand.FormAnswer(answer.fieldId(), answer.value()))
            .toList(),
        request.referenceAssets().stream()
            .map(asset -> new CreateOrderFormDraftCommand.ReferenceAsset(
                asset.assetId(), asset.source(), asset.sortOrder()))
            .toList());
  }
}
