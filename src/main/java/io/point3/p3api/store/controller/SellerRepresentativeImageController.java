package io.point3.p3api.store.controller;

import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.store.application.representative.command.CreateRepresentativeImageCommand;
import io.point3.p3api.store.application.representative.command.UpdateRepresentativeImageCommand;
import io.point3.p3api.store.application.representative.create.RepresentativeImageCreateUseCase;
import io.point3.p3api.store.application.representative.delete.RepresentativeImageDeleteUseCase;
import io.point3.p3api.store.application.representative.query.RepresentativeImageQueryUseCase;
import io.point3.p3api.store.application.representative.update.RepresentativeImageUpdateUseCase;
import io.point3.p3api.store.controller.request.RepresentativeImageCreateRequest;
import io.point3.p3api.store.controller.request.RepresentativeImageUpdateRequest;
import io.point3.p3api.store.controller.response.RepresentativeImageResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/representative-images")
@RequiredArgsConstructor
public class SellerRepresentativeImageController {
  private final RepresentativeImageCreateUseCase createUseCase;
  private final RepresentativeImageQueryUseCase queryUseCase;
  private final RepresentativeImageUpdateUseCase updateUseCase;
  private final RepresentativeImageDeleteUseCase deleteUseCase;

  @PostMapping
  public ApiResponse<RepresentativeImageResponse> create(
      @CurrentStoreId UUID storeId, @Valid @RequestBody RepresentativeImageCreateRequest request) {
    return ApiResponse.ok(RepresentativeImageResponse.from(createUseCase.create(
        new CreateRepresentativeImageCommand(storeId, request.assetId(), request.sortOrder()))));
  }

  @GetMapping
  public ApiResponse<List<RepresentativeImageResponse>> getImages(@CurrentStoreId UUID storeId) {
    return ApiResponse.ok(queryUseCase.getSellerImages(storeId).stream()
        .map(RepresentativeImageResponse::from)
        .toList());
  }

  @GetMapping("/{imageId}")
  public ApiResponse<RepresentativeImageResponse> getImage(
      @CurrentStoreId UUID storeId, @PathVariable UUID imageId) {
    return ApiResponse.ok(RepresentativeImageResponse.from(
        queryUseCase.getSellerImage(storeId, imageId)));
  }

  @PatchMapping("/{imageId}")
  public ApiResponse<RepresentativeImageResponse> update(
      @CurrentStoreId UUID storeId,
      @PathVariable UUID imageId,
      @Valid @RequestBody RepresentativeImageUpdateRequest request) {
    return ApiResponse.ok(RepresentativeImageResponse.from(updateUseCase.update(
        new UpdateRepresentativeImageCommand(
            storeId, imageId, request.sortOrder(), request.status()))));
  }

  @DeleteMapping("/{imageId}")
  public ApiResponse<Void> delete(@CurrentStoreId UUID storeId, @PathVariable UUID imageId) {
    deleteUseCase.delete(storeId, imageId);
    return ApiResponse.ok();
  }
}
