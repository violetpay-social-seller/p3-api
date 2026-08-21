package io.point3.p3api.gallery.controller;

import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.gallery.application.query.GalleryItemQueryUseCase;
import io.point3.p3api.gallery.controller.response.GalleryItemResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stores/{slug}/gallery-items")
@RequiredArgsConstructor
public class PublicGalleryController {

  private final GalleryItemQueryUseCase galleryItemQueryUseCase;

  @GetMapping
  public ApiResponse<List<GalleryItemResponse>> getItems(@CurrentStoreId UUID storeId) {
    return ApiResponse.ok(galleryItemQueryUseCase.getVisibleItems(storeId).stream()
        .map(GalleryItemResponse::from)
        .toList());
  }

  @GetMapping("/{galleryItemId}")
  public ApiResponse<GalleryItemResponse> getItem(
      @CurrentStoreId UUID storeId, @PathVariable UUID galleryItemId) {
    return ApiResponse.ok(
        GalleryItemResponse.from(galleryItemQueryUseCase.getVisibleItem(storeId, galleryItemId)));
  }
}
