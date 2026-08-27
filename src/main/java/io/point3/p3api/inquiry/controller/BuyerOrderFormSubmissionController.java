package io.point3.p3api.inquiry.controller;

import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.inquiry.application.submission.query.BuyerOrderFormSubmissionQueryUseCase;
import io.point3.p3api.order.controller.response.OrderFormSubmissionResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inquiries/{inquiryId}/order-form-submissions")
@RequiredArgsConstructor
public class BuyerOrderFormSubmissionController {

  private final BuyerOrderFormSubmissionQueryUseCase buyerOrderFormSubmissionQueryUseCase;

  @GetMapping("/{submissionId}")
  public ApiResponse<OrderFormSubmissionResponse> getSubmission(
      @PathVariable UUID inquiryId,
      @PathVariable UUID submissionId,
      @Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);
    return ApiResponse.ok(
        OrderFormSubmissionResponse.from(buyerOrderFormSubmissionQueryUseCase.getSubmission(
            inquiryId, submissionId, currentUser.userId())));
  }
}
