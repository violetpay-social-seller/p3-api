package io.point3.p3api.store.controller;

import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.store.application.query.StoreQueryUseCase;
import io.point3.p3api.store.application.representative.query.RepresentativeImageQueryUseCase;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.store.application.setting.availability.StoreOrderSettingAvailabilityQueryUseCase;
import io.point3.p3api.store.controller.response.RepresentativeImageResponse;
import io.point3.p3api.store.controller.response.StoreOrderSettingAvailabilityResponse;
import io.point3.p3api.store.controller.response.StoreResponse;
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

  private final StoreQueryUseCase storeQueryUseCase;
  private final RepresentativeImageQueryUseCase representativeImageQueryUseCase;
  private final StoreOrderSettingAvailabilityQueryUseCase availabilityQueryUseCase;

  @GetMapping("/{slug}")
  public ApiResponse<StoreResponse> getStore(
      @PathVariable String slug, @CurrentStoreId UUID storeId) {
    StoreResult result = storeQueryUseCase.getStore(storeId);
    return ApiResponse.ok(StoreResponse.from(result));
  }

  @GetMapping("/{slug}/representative-images")
  public ApiResponse<List<RepresentativeImageResponse>> getRepresentativeImages(
      @PathVariable String slug, @CurrentStoreId UUID storeId) {
    return ApiResponse.ok(representativeImageQueryUseCase.getActiveImages(storeId).stream()
        .map(RepresentativeImageResponse::from)
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
