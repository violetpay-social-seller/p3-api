package io.point3.p3api.inquiry.controller;

import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.inquiry.application.command.OpenInquiryCommand;
import io.point3.p3api.inquiry.application.open.OpenInquiryUseCase;
import io.point3.p3api.inquiry.controller.response.InquiryOpenResponse;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stores/{slug}/inquiries")
@RequiredArgsConstructor
public class InquiryOpenController {

  private final OpenInquiryUseCase openInquiryUseCase;

  @PostMapping("/open")
  public ApiResponse<InquiryOpenResponse> open(
      @PathVariable String slug,
      @CurrentStoreId UUID storeId,
      @Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);

    Inquiry inquiry = openInquiryUseCase.open(OpenInquiryCommand.of(storeId, currentUser.userId()));
    return ApiResponse.ok(InquiryOpenResponse.from(inquiry));
  }
}
