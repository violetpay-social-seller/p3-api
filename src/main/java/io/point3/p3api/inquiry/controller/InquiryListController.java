package io.point3.p3api.inquiry.controller;

import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.inquiry.application.list.InquiryListUseCase;
import io.point3.p3api.inquiry.controller.response.InquiryListItemResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InquiryListController {
  private final InquiryListUseCase inquiryListUseCase;

  @GetMapping("/inquiries")
  public ApiResponse<List<InquiryListItemResponse>> getBuyerInquiries(
      @Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);
    return ApiResponse.ok(inquiryListUseCase.getBuyerInquiries(currentUser.userId()).stream()
        .map(InquiryListItemResponse::from)
        .toList());
  }

  @PatchMapping("/inquiries/{inquiryId}/read")
  public ApiResponse<Void> markBuyerRead(
      @PathVariable UUID inquiryId, @Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);
    inquiryListUseCase.markBuyerRead(inquiryId, currentUser.userId());
    return ApiResponse.ok();
  }

  @GetMapping("/seller/inquiries")
  public ApiResponse<List<InquiryListItemResponse>> getSellerInquiries(
      @CurrentStoreId UUID storeId, @Authenticated CurrentUser currentUser) {
    return ApiResponse.ok(
        inquiryListUseCase.getSellerInquiries(storeId, currentUser.userId()).stream()
            .map(InquiryListItemResponse::from)
            .toList());
  }

  @PatchMapping("/seller/inquiries/{inquiryId}/read")
  public ApiResponse<Void> markSellerRead(
      @PathVariable UUID inquiryId, @CurrentStoreId UUID storeId) {
    inquiryListUseCase.markSellerRead(inquiryId, storeId);
    return ApiResponse.ok();
  }
}
