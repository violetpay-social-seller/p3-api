package io.point3.p3api.inquiry.controller;

import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.inquiry.application.command.RequestOrderFormRevisionCommand;
import io.point3.p3api.inquiry.application.submission.query.SellerOrderFormSubmissionQueryUseCase;
import io.point3.p3api.inquiry.application.submission.revision.OrderFormRevisionRequestUseCase;
import io.point3.p3api.inquiry.application.submission.view.SellerOrderFormSubmissionViewUseCase;
import io.point3.p3api.inquiry.controller.response.ChatTimelineItemResponse;
import io.point3.p3api.order.controller.response.OrderFormSubmissionResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/inquiries/{inquiryId}/order-form-submissions")
@RequiredArgsConstructor
public class SellerOrderFormSubmissionController {

  private final SellerOrderFormSubmissionQueryUseCase sellerOrderFormSubmissionQueryUseCase;
  private final OrderFormRevisionRequestUseCase orderFormRevisionRequestUseCase;
  private final SellerOrderFormSubmissionViewUseCase sellerOrderFormSubmissionViewUseCase;

  @GetMapping("/{submissionId}")
  public ApiResponse<OrderFormSubmissionResponse> getSubmission(
      @PathVariable UUID inquiryId, @PathVariable UUID submissionId, @CurrentStoreId UUID storeId) {
    return ApiResponse.ok(OrderFormSubmissionResponse.from(
        sellerOrderFormSubmissionQueryUseCase.getSubmission(inquiryId, submissionId, storeId)));
  }

  @GetMapping
  public ApiResponse<List<OrderFormSubmissionResponse>> getSubmissions(
      @PathVariable UUID inquiryId, @CurrentStoreId UUID storeId) {
    return ApiResponse.ok(
        sellerOrderFormSubmissionQueryUseCase.getSubmissions(inquiryId, storeId).stream()
            .map(OrderFormSubmissionResponse::from)
            .toList());
  }

  @PostMapping("/{submissionId}/view")
  public ApiResponse<OrderFormSubmissionResponse> markViewed(
      @PathVariable UUID inquiryId, @PathVariable UUID submissionId, @CurrentStoreId UUID storeId) {
    return ApiResponse.ok(OrderFormSubmissionResponse.from(
        sellerOrderFormSubmissionViewUseCase.markViewed(inquiryId, submissionId, storeId)));
  }

  @PostMapping("/{submissionId}/revision-request")
  public ApiResponse<ChatTimelineItemResponse> requestRevision(
      @PathVariable UUID inquiryId,
      @PathVariable UUID submissionId,
      @CurrentStoreId UUID storeId,
      @Authenticated CurrentUser currentUser) {
    RoleGuard.requireSeller(currentUser);
    return ApiResponse.ok(ChatTimelineItemResponse.from(
        orderFormRevisionRequestUseCase.requestRevision(RequestOrderFormRevisionCommand.of(
            inquiryId, submissionId, storeId, currentUser.userId()))));
  }
}
