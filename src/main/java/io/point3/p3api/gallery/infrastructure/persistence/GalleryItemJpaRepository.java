package io.point3.p3api.gallery.infrastructure.persistence;

import io.point3.p3api.gallery.domain.entity.StoreGalleryItem;
import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GalleryItemJpaRepository extends JpaRepository<StoreGalleryItem, UUID> {

  Optional<StoreGalleryItem> findByIdAndStoreId(UUID galleryItemId, UUID storeId);

  Optional<StoreGalleryItem> findByAssetIdAndStoreIdAndStatus(
      UUID assetId, UUID storeId, StoreGalleryItemStatus status);

  List<StoreGalleryItem> findAllByStoreIdOrderBySortOrderAsc(UUID storeId);

  List<StoreGalleryItem> findAllByStoreIdAndStatusOrderBySortOrderAsc(
      UUID storeId, StoreGalleryItemStatus status);
}
