package io.point3.p3api.inquiry.controller;

import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.inquiry.application.submission.query.SellerOrderFormSubmissionQueryUseCase;
import io.point3.p3api.order.controller.response.OrderFormSubmissionResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/inquiries/{inquiryId}/order-form-submissions")
@RequiredArgsConstructor
public class SellerOrderFormSubmissionController {

  private final SellerOrderFormSubmissionQueryUseCase sellerOrderFormSubmissionQueryUseCase;

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
}
