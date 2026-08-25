package io.point3.p3api.payment.controller;

import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.payment.application.capture.CapturePaymentCommand;
import io.point3.p3api.payment.application.capture.PaymentCaptureUseCase;
import io.point3.p3api.payment.application.prepare.PaymentPrepareUseCase;
import io.point3.p3api.payment.application.prepare.PreparePaymentCommand;
import io.point3.p3api.payment.controller.request.PaymentCaptureRequest;
import io.point3.p3api.payment.controller.response.PaymentCaptureResponse;
import io.point3.p3api.payment.controller.response.PaymentPreparationResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BuyerPaymentController {

  private final PaymentPrepareUseCase paymentPrepareUseCase;
  private final PaymentCaptureUseCase paymentCaptureUseCase;

  @PostMapping("/inquiries/{inquiryId}/confirmations/{confirmationId}/payment-attempts")
  public ApiResponse<PaymentPreparationResponse> prepare(
      @PathVariable UUID inquiryId,
      @PathVariable UUID confirmationId,
      @Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);
    return ApiResponse.ok(PaymentPreparationResponse.from(paymentPrepareUseCase.prepare(
        PreparePaymentCommand.of(inquiryId, confirmationId, currentUser.userId()))));
  }

  @PostMapping("/payment-attempts/{paymentAttemptId}/capture")
  public ApiResponse<PaymentCaptureResponse> capture(
      @PathVariable UUID paymentAttemptId,
      @Valid @RequestBody PaymentCaptureRequest request,
      @Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);
    return ApiResponse.ok(
        PaymentCaptureResponse.from(paymentCaptureUseCase.capture(CapturePaymentCommand.of(
            paymentAttemptId, currentUser.userId(), request.sessionId(), request.payerId()))));
  }
}
