package io.point3.p3api.store.controller;

import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.store.application.publicquery.PublicStoreListQuery;
import io.point3.p3api.store.application.publicquery.PublicStoreQueryUseCase;
import io.point3.p3api.store.application.setting.availability.StoreOrderSettingAvailabilityQueryUseCase;
import io.point3.p3api.store.controller.response.PublicRepresentativeImageResponse;
import io.point3.p3api.store.controller.response.PublicStorePageResponse;
import io.point3.p3api.store.controller.response.PublicStoreResponse;
import io.point3.p3api.store.controller.response.StoreOrderSettingAvailabilityResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class PublicStoreController {

  private final PublicStoreQueryUseCase publicStoreQueryUseCase;
  private final StoreOrderSettingAvailabilityQueryUseCase availabilityQueryUseCase;

  @GetMapping
  public ApiResponse<PublicStorePageResponse> getStores(
      @RequestParam(required = false) Instant cursorUpdatedAt,
      @RequestParam(required = false) UUID cursorId,
      @RequestParam(required = false) Integer size) {
    return ApiResponse.ok(PublicStorePageResponse.from(publicStoreQueryUseCase.getStores(
        new PublicStoreListQuery(cursorUpdatedAt, cursorId, size))));
  }

  @GetMapping("/{slug}")
  public ApiResponse<PublicStoreResponse> getStore(
      @PathVariable String slug, @CurrentStoreId UUID storeId) {
    return ApiResponse.ok(PublicStoreResponse.from(publicStoreQueryUseCase.getStore(storeId)));
  }

  @GetMapping("/{slug}/representative-images")
  public ApiResponse<List<PublicRepresentativeImageResponse>> getRepresentativeImages(
      @PathVariable String slug, @CurrentStoreId UUID storeId) {
    return ApiResponse.ok(publicStoreQueryUseCase.getStore(storeId).representativeImages().stream()
        .map(PublicRepresentativeImageResponse::from)
        .toList());
  }

  @GetMapping("/{slug}/order-settings")
  public ApiResponse<StoreOrderSettingAvailabilityResponse> getOrderSettings(
      @PathVariable String slug,
      @CurrentStoreId UUID storeId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return ApiResponse.ok(StoreOrderSettingAvailabilityResponse.from(
        availabilityQueryUseCase.getAvailability(storeId, from, to)));
  }
}
