package io.point3.p3api.store.notice.controller;

import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.store.notice.application.query.StoreNoticeQueryUseCase;
import io.point3.p3api.store.notice.application.result.StoreNoticeResult;
import io.point3.p3api.store.notice.application.update.StoreNoticeUpdateUseCase;
import io.point3.p3api.store.notice.controller.request.StoreNoticeUpdateRequest;
import io.point3.p3api.store.notice.controller.response.StoreNoticeResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/notices")
@RequiredArgsConstructor
public class SellerStoreNoticeController {

  private final StoreNoticeQueryUseCase storeNoticeQueryUseCase;
  private final StoreNoticeUpdateUseCase storeNoticeUpdateUseCase;

  @GetMapping
  public ApiResponse<StoreNoticeResponse> getNotices(@CurrentStoreId UUID storeId) {
    StoreNoticeResult result = storeNoticeQueryUseCase.getNotices(storeId);
    return ApiResponse.ok(StoreNoticeResponse.from(result));
  }

  @PutMapping
  public ApiResponse<StoreNoticeResponse> updateNotices(
      @CurrentStoreId UUID storeId, @Valid @RequestBody StoreNoticeUpdateRequest request) {
    StoreNoticeResult result = storeNoticeUpdateUseCase.update(request.toCommand(storeId));
    return ApiResponse.ok(StoreNoticeResponse.from(result));
  }
}
