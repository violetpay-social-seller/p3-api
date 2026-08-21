package io.point3.p3api.gallery.controller;

import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.gallery.application.command.CreateGalleryItemCommand;
import io.point3.p3api.gallery.application.command.UpdateGalleryItemCommand;
import io.point3.p3api.gallery.application.create.GalleryItemCreateUseCase;
import io.point3.p3api.gallery.application.delete.GalleryItemDeleteUseCase;
import io.point3.p3api.gallery.application.query.GalleryItemQueryUseCase;
import io.point3.p3api.gallery.application.update.GalleryItemUpdateUseCase;
import io.point3.p3api.gallery.controller.request.GalleryItemCreateRequest;
import io.point3.p3api.gallery.controller.request.GalleryItemUpdateRequest;
import io.point3.p3api.gallery.controller.response.GalleryItemResponse;
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
@RequestMapping("/seller/gallery-items")
@RequiredArgsConstructor
public class SellerGalleryController {

  private final GalleryItemCreateUseCase galleryItemCreateUseCase;
  private final GalleryItemQueryUseCase galleryItemQueryUseCase;
  private final GalleryItemUpdateUseCase galleryItemUpdateUseCase;
  private final GalleryItemDeleteUseCase galleryItemDeleteUseCase;

  @PostMapping
  public ApiResponse<GalleryItemResponse> create(
      @CurrentStoreId UUID storeId, @Valid @RequestBody GalleryItemCreateRequest request) {
    return ApiResponse.ok(
        GalleryItemResponse.from(galleryItemCreateUseCase.create(toCommand(storeId, request))));
  }

  @GetMapping
  public ApiResponse<List<GalleryItemResponse>> getItems(@CurrentStoreId UUID storeId) {
    return ApiResponse.ok(galleryItemQueryUseCase.getSellerItems(storeId).stream()
        .map(GalleryItemResponse::from)
        .toList());
  }

  @GetMapping("/{galleryItemId}")
  public ApiResponse<GalleryItemResponse> getItem(
      @CurrentStoreId UUID storeId, @PathVariable UUID galleryItemId) {
    return ApiResponse.ok(
        GalleryItemResponse.from(galleryItemQueryUseCase.getSellerItem(storeId, galleryItemId)));
  }

  @PatchMapping("/{galleryItemId}")
  public ApiResponse<GalleryItemResponse> update(
      @CurrentStoreId UUID storeId,
      @PathVariable UUID galleryItemId,
      @Valid @RequestBody GalleryItemUpdateRequest request) {
    return ApiResponse.ok(GalleryItemResponse.from(
        galleryItemUpdateUseCase.update(toCommand(storeId, galleryItemId, request))));
  }

  @DeleteMapping("/{galleryItemId}")
  public ApiResponse<Void> delete(@CurrentStoreId UUID storeId, @PathVariable UUID galleryItemId) {
    galleryItemDeleteUseCase.delete(storeId, galleryItemId);
    return ApiResponse.ok();
  }

  private CreateGalleryItemCommand toCommand(UUID storeId, GalleryItemCreateRequest request) {
    return new CreateGalleryItemCommand(
        storeId, request.assetId(), request.sortOrder(), request.featured());
  }

  private UpdateGalleryItemCommand toCommand(
      UUID storeId, UUID galleryItemId, GalleryItemUpdateRequest request) {
    return new UpdateGalleryItemCommand(
        storeId, galleryItemId, request.sortOrder(), request.featured(), request.status());
  }
}
