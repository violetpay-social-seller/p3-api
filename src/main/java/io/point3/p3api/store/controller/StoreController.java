package io.point3.p3api.store.controller;

import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.create.StoreCreateUseCase;
import io.point3.p3api.store.application.delete.StoreDeleteUseCase;
import io.point3.p3api.store.application.query.StoreQueryUseCase;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.store.application.update.ChangeStoreStatusCommand;
import io.point3.p3api.store.application.update.StoreUpdateUseCase;
import io.point3.p3api.store.application.update.UpdateStoreCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

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

  @GetMapping("/me")
  public ApiResponse<StoreResponse> getMyStore(@Authenticated CurrentUser currentUser) {
    StoreResult result = storeQueryUseCase.getMyStore(currentUser.userId());
    return ApiResponse.ok(StoreResponse.from(result));
  }

  @PatchMapping("/me")
  public ApiResponse<StoreResponse> update(
      @Authenticated CurrentUser currentUser, @Valid @RequestBody StoreUpdateRequest request) {
    StoreResult result = storeUpdateUseCase.update(toCommand(currentUser, request));
    return ApiResponse.ok(StoreResponse.from(result));
  }

  @PatchMapping("/me/status")
  public ApiResponse<StoreResponse> changeStatus(
      @Authenticated CurrentUser currentUser, @Valid @RequestBody StoreStatusRequest request) {
    StoreResult result = storeUpdateUseCase.changeStatus(
        new ChangeStoreStatusCommand(currentUser.userId(), request.status()));
    return ApiResponse.ok(StoreResponse.from(result));
  }

  @DeleteMapping("/me")
  public ApiResponse<Void> deleteMyStore(@Authenticated CurrentUser currentUser) {
    storeDeleteUseCase.deleteMyStore(currentUser.userId());
    return ApiResponse.ok();
  }

  private CreateStoreCommand toCommand(CurrentUser currentUser, StoreCreateRequest request) {
    return new CreateStoreCommand(
        currentUser.userId(),
        request.name(),
        request.profileAssetId(),
        request.bannerAssetId(),
        request.description(),
        request.contact(),
        request.contactVisible(),
        request.snsLinks(),
        request.businessHours(),
        request.address());
  }

  private UpdateStoreCommand toCommand(CurrentUser currentUser, StoreUpdateRequest request) {
    return new UpdateStoreCommand(
        currentUser.userId(),
        request.name(),
        request.profileAssetId(),
        request.bannerAssetId(),
        request.description(),
        request.contact(),
        request.contactVisible(),
        request.snsLinks(),
        request.businessHours(),
        request.address());
  }
}
