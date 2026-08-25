package io.point3.p3api.gallery.infrastructure.persistence;

import io.point3.p3api.gallery.application.port.GalleryItemPersistencePort;
import io.point3.p3api.gallery.domain.entity.StoreGalleryItem;
import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class GalleryItemPersistenceAdapter implements GalleryItemPersistencePort {

  private final GalleryItemJpaRepository galleryItemJpaRepository;

  @Override
  public StoreGalleryItem save(StoreGalleryItem item) {
    return galleryItemJpaRepository.save(item);
  }

  @Override
  public Optional<StoreGalleryItem> findByIdAndStoreId(UUID galleryItemId, UUID storeId) {
    return galleryItemJpaRepository.findByIdAndStoreId(galleryItemId, storeId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<StoreGalleryItem> findVisibleByAssetIdAndStoreId(UUID assetId, UUID storeId) {
    return galleryItemJpaRepository.findByAssetIdAndStoreIdAndStatus(
        assetId, storeId, StoreGalleryItemStatus.VISIBLE);
  }

  @Override
  public List<StoreGalleryItem> findAllByStoreId(UUID storeId) {
    return galleryItemJpaRepository.findAllByStoreIdOrderBySortOrderAsc(storeId);
  }

  @Override
  public List<StoreGalleryItem> findVisibleByStoreId(UUID storeId) {
    return galleryItemJpaRepository.findAllByStoreIdAndStatusOrderBySortOrderAsc(
        storeId, StoreGalleryItemStatus.VISIBLE);
  }

  @Override
  public void delete(StoreGalleryItem item) {
    galleryItemJpaRepository.delete(item);
  }
}
