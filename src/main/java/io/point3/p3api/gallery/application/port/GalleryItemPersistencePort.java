package io.point3.p3api.gallery.application.port;

import io.point3.p3api.gallery.domain.entity.StoreGalleryItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GalleryItemPersistencePort {

  StoreGalleryItem save(StoreGalleryItem item);

  Optional<StoreGalleryItem> findByIdAndStoreId(UUID galleryItemId, UUID storeId);

  Optional<StoreGalleryItem> findVisibleByAssetIdAndStoreId(UUID assetId, UUID storeId);

  List<StoreGalleryItem> findAllByStoreId(UUID storeId);

  List<StoreGalleryItem> findVisibleByStoreId(UUID storeId);

  void delete(StoreGalleryItem item);
}
