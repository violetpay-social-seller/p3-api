package io.point3.p3api.gallery.application.query;

import io.point3.p3api.gallery.application.result.GalleryItemResult;
import java.util.List;
import java.util.UUID;

public interface GalleryItemQueryUseCase {

  List<GalleryItemResult> getSellerItems(UUID storeId);

  GalleryItemResult getSellerItem(UUID storeId, UUID galleryItemId);

  List<GalleryItemResult> getVisibleItems(UUID storeId);

  GalleryItemResult getVisibleItem(UUID storeId, UUID galleryItemId);
}
