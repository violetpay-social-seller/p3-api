package io.point3.p3api.inquiry.controller;

import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.inquiry.application.list.InquiryListUseCase;
import io.point3.p3api.inquiry.controller.response.InquiryListItemResponse;
import io.point3.p3api.inquiry.domain.type.InquiryStatus;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class InquiryListController {
  private final InquiryListUseCase inquiryListUseCase;

  @GetMapping("/inquiries")
  public ApiResponse<List<InquiryListItemResponse>> getBuyerInquiries(
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) InquiryStatus status,
      @RequestParam(defaultValue = "false") boolean unreadOnly) {
    RoleGuard.requireBuyer(currentUser);
    return ApiResponse.ok(
        inquiryListUseCase.getBuyerInquiries(currentUser.userId(), status, unreadOnly).stream()
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

  @PatchMapping("/inquiries/{inquiryId}/trash")
  public ApiResponse<Void> moveBuyerToTrash(
      @PathVariable UUID inquiryId, @Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);
    inquiryListUseCase.moveBuyerToTrash(inquiryId, currentUser.userId());
    return ApiResponse.ok();
  }

  @PatchMapping("/inquiries/{inquiryId}/restore")
  public ApiResponse<Void> restoreBuyerFromTrash(
      @PathVariable UUID inquiryId, @Authenticated CurrentUser currentUser) {
    RoleGuard.requireBuyer(currentUser);
    inquiryListUseCase.restoreBuyerFromTrash(inquiryId, currentUser.userId());
    return ApiResponse.ok();
  }

  @GetMapping("/seller/inquiries")
  public ApiResponse<List<InquiryListItemResponse>> getSellerInquiries(
      @CurrentStoreId UUID storeId,
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) InquiryStatus status,
      @RequestParam(defaultValue = "false") boolean unreadOnly) {
    return ApiResponse.ok(
        inquiryListUseCase
            .getSellerInquiries(storeId, currentUser.userId(), status, unreadOnly)
            .stream()
            .map(InquiryListItemResponse::from)
            .toList());
  }

  @PatchMapping("/seller/inquiries/{inquiryId}/read")
  public ApiResponse<Void> markSellerRead(
      @PathVariable UUID inquiryId, @CurrentStoreId UUID storeId) {
    inquiryListUseCase.markSellerRead(inquiryId, storeId);
    return ApiResponse.ok();
  }

  @PatchMapping("/seller/inquiries/{inquiryId}/trash")
  public ApiResponse<Void> moveSellerToTrash(
      @PathVariable UUID inquiryId, @CurrentStoreId UUID storeId) {
    inquiryListUseCase.moveSellerToTrash(inquiryId, storeId);
    return ApiResponse.ok();
  }

  @PatchMapping("/seller/inquiries/{inquiryId}/restore")
  public ApiResponse<Void> restoreSellerFromTrash(
      @PathVariable UUID inquiryId, @CurrentStoreId UUID storeId) {
    inquiryListUseCase.restoreSellerFromTrash(inquiryId, storeId);
    return ApiResponse.ok();
  }
}
