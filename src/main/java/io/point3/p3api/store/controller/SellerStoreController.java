package io.point3.p3api.store.controller;

import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.create.StoreCreateUseCase;
import io.point3.p3api.store.application.delete.StoreDeleteUseCase;
import io.point3.p3api.store.application.query.StoreQueryUseCase;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.store.application.update.ChangeStoreStatusCommand;
import io.point3.p3api.store.application.update.StoreUpdateUseCase;
import io.point3.p3api.store.application.update.UpdateStoreCommand;
import io.point3.p3api.store.controller.request.StoreCreateRequest;
import io.point3.p3api.store.controller.request.StoreStatusRequest;
import io.point3.p3api.store.controller.request.StoreUpdateRequest;
import io.point3.p3api.store.controller.response.StoreResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/store")
@RequiredArgsConstructor
public class SellerStoreController {

  private final StoreCreateUseCase storeCreateUseCase;
  private final StoreQueryUseCase storeQueryUseCase;
  private final StoreUpdateUseCase storeUpdateUseCase;
  private final StoreDeleteUseCase storeDeleteUseCase;

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

  @PatchMapping
  public ApiResponse<StoreResponse> update(
      @CurrentStoreId UUID storeId, @Valid @RequestBody StoreUpdateRequest request) {
    StoreResult result = storeUpdateUseCase.update(toCommand(storeId, request));
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
        request.pickupSettings(),
        request.address());
  }
}
