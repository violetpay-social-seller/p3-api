package io.point3.p3api.store.controller;

import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.store.application.businesshours.query.StoreBusinessHoursQueryUseCase;
import io.point3.p3api.store.application.businesshours.update.StoreBusinessHoursUpdateUseCase;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.create.StoreCreateUseCase;
import io.point3.p3api.store.application.delete.StoreDeleteUseCase;
import io.point3.p3api.store.application.location.command.SearchStoreLocationCommand;
import io.point3.p3api.store.application.location.query.StoreLocationSearchUseCase;
import io.point3.p3api.store.application.location.result.StoreLocationResult;
import io.point3.p3api.store.application.management.StoreManagementStatusQueryUseCase;
import io.point3.p3api.store.application.profileimage.SellerProfileImageUpdateUseCase;
import io.point3.p3api.store.application.profileimage.UpdateSellerProfileImageCommand;
import io.point3.p3api.store.application.query.StoreQueryUseCase;
import io.point3.p3api.store.application.refundpolicy.query.StoreRefundPolicyQueryUseCase;
import io.point3.p3api.store.application.refundpolicy.update.StoreRefundPolicyUpdateUseCase;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.store.application.setting.query.StoreSettingQueryUseCase;
import io.point3.p3api.store.application.setting.update.StoreSettingUpdateUseCase;
import io.point3.p3api.store.application.update.ChangeStoreStatusCommand;
import io.point3.p3api.store.application.update.CompleteAccountRegistrationCommand;
import io.point3.p3api.store.application.update.StoreUpdateUseCase;
import io.point3.p3api.store.application.update.UpdateStoreCommand;
import io.point3.p3api.store.controller.request.StoreBusinessHoursRequest;
import io.point3.p3api.store.controller.request.StoreCreateRequest;
import io.point3.p3api.store.controller.request.StoreDescriptionRequest;
import io.point3.p3api.store.controller.request.StoreProfileImageRequest;
import io.point3.p3api.store.controller.request.StoreRefundPolicyRequest;
import io.point3.p3api.store.controller.request.StoreSettingRequest;
import io.point3.p3api.store.controller.request.StoreStatusRequest;
import io.point3.p3api.store.controller.request.StoreUpdateRequest;
import io.point3.p3api.store.controller.response.SellerProfileImageResponse;
import io.point3.p3api.store.controller.response.StoreBusinessHoursResponse;
import io.point3.p3api.store.controller.response.StoreLocationSearchResponse;
import io.point3.p3api.store.controller.response.StoreManagementStatusResponse;
import io.point3.p3api.store.controller.response.StoreRefundPolicyResponse;
import io.point3.p3api.store.controller.response.StoreResponse;
import io.point3.p3api.store.controller.response.StoreSettingResponse;
import io.point3.p3api.store.controller.response.StoreShareLinkResponse;
import io.point3.p3api.store.infrastructure.web.StoreWebProperties;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/store")
@RequiredArgsConstructor
public class SellerStoreController {

  private final StoreCreateUseCase storeCreateUseCase;
  private final StoreQueryUseCase storeQueryUseCase;
  private final StoreUpdateUseCase storeUpdateUseCase;
  private final StoreDeleteUseCase storeDeleteUseCase;
  private final StoreBusinessHoursQueryUseCase storeBusinessHoursQueryUseCase;
  private final StoreBusinessHoursUpdateUseCase storeBusinessHoursUpdateUseCase;
  private final StoreManagementStatusQueryUseCase storeManagementStatusQueryUseCase;
  private final StoreSettingQueryUseCase storeSettingQueryUseCase;
  private final StoreSettingUpdateUseCase storeSettingUpdateUseCase;
  private final StoreRefundPolicyQueryUseCase storeRefundPolicyQueryUseCase;
  private final StoreRefundPolicyUpdateUseCase storeRefundPolicyUpdateUseCase;
  private final StoreLocationSearchUseCase storeLocationSearchUseCase;
  private final SellerProfileImageUpdateUseCase sellerProfileImageUpdateUseCase;
  private final StoreWebProperties storeWebProperties;

  @PostMapping
  public ApiResponse<StoreResponse> create(
      @Authenticated CurrentUser currentUser, @Valid @RequestBody StoreCreateRequest request) {
    StoreResult result = storeCreateUseCase.create(toCommand(currentUser, request));
    return ApiResponse.ok(StoreResponse.from(result));
  }

  @GetMapping
  public ApiResponse<StoreResponse> getMyStore(@CurrentStoreId UUID storeId) {
    StoreResult result = storeQueryUseCase.getStore(storeId);
    return ApiResponse.ok(StoreResponse.from(result));
  }

  @GetMapping("/management-status")
  public ApiResponse<StoreManagementStatusResponse> getManagementStatus(
      @CurrentStoreId UUID storeId) {
    return ApiResponse.ok(
        StoreManagementStatusResponse.from(storeManagementStatusQueryUseCase.getStatus(storeId)));
  }

  @GetMapping("/settings")
  public ApiResponse<StoreSettingResponse> getSettings(@CurrentStoreId UUID storeId) {
    return ApiResponse.ok(StoreSettingResponse.from(storeSettingQueryUseCase.getSetting(storeId)));
  }

  @GetMapping("/business-hours")
  public ApiResponse<StoreBusinessHoursResponse> getBusinessHours(@CurrentStoreId UUID storeId) {
    return ApiResponse.ok(
        StoreBusinessHoursResponse.from(storeBusinessHoursQueryUseCase.getBusinessHours(storeId)));
  }

  @PutMapping("/business-hours")
  public ApiResponse<StoreBusinessHoursResponse> updateBusinessHours(
      @CurrentStoreId UUID storeId, @Valid @RequestBody StoreBusinessHoursRequest request) {
    return ApiResponse.ok(StoreBusinessHoursResponse.from(
        storeBusinessHoursUpdateUseCase.updateBusinessHours(request.toCommand(storeId))));
  }

  @PutMapping("/settings")
  public ApiResponse<StoreSettingResponse> updateSettings(
      @CurrentStoreId UUID storeId, @Valid @RequestBody StoreSettingRequest request) {
    return ApiResponse.ok(
        StoreSettingResponse.from(storeSettingUpdateUseCase.update(request.toCommand(storeId))));
  }

  @GetMapping("/refund-policy")
  public ApiResponse<StoreRefundPolicyResponse> getRefundPolicy(@CurrentStoreId UUID storeId) {
    return ApiResponse.ok(
        StoreRefundPolicyResponse.from(storeRefundPolicyQueryUseCase.getRefundPolicy(storeId)));
  }

  @PutMapping("/refund-policy")
  public ApiResponse<StoreRefundPolicyResponse> updateRefundPolicy(
      @CurrentStoreId UUID storeId, @Valid @RequestBody StoreRefundPolicyRequest request) {
    return ApiResponse.ok(StoreRefundPolicyResponse.from(
        storeRefundPolicyUpdateUseCase.updateRefundPolicy(request.toCommand(storeId))));
  }

  @GetMapping("/share-link")
  public ApiResponse<StoreShareLinkResponse> getShareLink(@CurrentStoreId UUID storeId) {
    StoreResult store = storeQueryUseCase.getStore(storeId);
    return ApiResponse.ok(StoreShareLinkResponse.from(store, publicStoreUrl(store.slug())));
  }

  @GetMapping("/locations/search")
  public ApiResponse<StoreLocationSearchResponse> searchLocations(
      @Authenticated CurrentUser currentUser, @RequestParam String query) {
    RoleGuard.requireSeller(currentUser);
    List<StoreLocationResult> results =
        storeLocationSearchUseCase.search(SearchStoreLocationCommand.of(query));
    return ApiResponse.ok(StoreLocationSearchResponse.from(results));
  }

  @PatchMapping
  public ApiResponse<StoreResponse> update(
      @CurrentStoreId UUID storeId, @Valid @RequestBody StoreUpdateRequest request) {
    StoreResult result = storeUpdateUseCase.update(toCommand(storeId, request));
    return ApiResponse.ok(StoreResponse.from(result));
  }

  @PatchMapping("/profile-image")
  public ApiResponse<SellerProfileImageResponse> updateProfileImage(
      @CurrentStoreId UUID storeId,
      @Authenticated CurrentUser currentUser,
      @RequestBody StoreProfileImageRequest request) {
    return ApiResponse.ok(SellerProfileImageResponse.from(
        sellerProfileImageUpdateUseCase.update(new UpdateSellerProfileImageCommand(
            storeId, currentUser.userId(), request.profileAssetId()))));
  }

  @PutMapping("/description")
  public ApiResponse<StoreResponse> updateDescription(
      @CurrentStoreId UUID storeId, @Valid @RequestBody StoreDescriptionRequest request) {
    StoreResult result = storeUpdateUseCase.updateDescription(request.toCommand(storeId));
    return ApiResponse.ok(StoreResponse.from(result));
  }

  @PostMapping("/account-registration/complete")
  public ApiResponse<StoreResponse> completeAccountRegistration(@CurrentStoreId UUID storeId) {
    StoreResult result = storeUpdateUseCase.completeAccountRegistration(
        new CompleteAccountRegistrationCommand(storeId));
    return ApiResponse.ok(StoreResponse.from(result));
  }

  @PatchMapping("/status")
  public ApiResponse<StoreResponse> changeStatus(
      @CurrentStoreId UUID storeId, @Valid @RequestBody StoreStatusRequest request) {
    StoreResult result =
        storeUpdateUseCase.changeStatus(new ChangeStoreStatusCommand(storeId, request.status()));
    return ApiResponse.ok(StoreResponse.from(result));
  }

  @DeleteMapping
  public ApiResponse<Void> deleteMyStore(@CurrentStoreId UUID storeId) {
    storeDeleteUseCase.delete(storeId);
    return ApiResponse.ok();
  }

  private CreateStoreCommand toCommand(CurrentUser currentUser, StoreCreateRequest request) {
    return new CreateStoreCommand(
        currentUser.userId(),
        request.name(),
        request.profileAssetId(),
        request.description(),
        request.contact(),
        request.contactVisible(),
        request.snsLinks(),
        request.businessHours(),
        request.pickupSettings(),
        request.address());
  }

  private UpdateStoreCommand toCommand(UUID storeId, StoreUpdateRequest request) {
    return new UpdateStoreCommand(
        storeId,
        request.name(),
        request.profileAssetId(),
        request.description(),
        request.contact(),
        request.contactVisible(),
        request.snsLinks(),
        request.businessHours(),
        request.pickupSettings());
  }

  private String publicStoreUrl(String slug) {
    String baseUrl = storeWebProperties.baseUrl();
    if (baseUrl.endsWith("/")) {
      baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    }
    return baseUrl + "/stores/" + slug;
  }
}
