package io.point3.p3api.gallery.application;

import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.assetvariant.application.AssetVariantDeliveryService;
import io.point3.p3api.assetvariant.application.result.AssetVariantDelivery;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.GalleryErrorCode;
import io.point3.p3api.gallery.application.command.CreateGalleryItemCommand;
import io.point3.p3api.gallery.application.command.UpdateGalleryItemCommand;
import io.point3.p3api.gallery.application.create.GalleryItemCreateUseCase;
import io.point3.p3api.gallery.application.delete.GalleryItemDeleteUseCase;
import io.point3.p3api.gallery.application.port.GalleryItemPersistencePort;
import io.point3.p3api.gallery.application.query.GalleryItemQueryUseCase;
import io.point3.p3api.gallery.application.result.GalleryItemResult;
import io.point3.p3api.gallery.application.update.GalleryItemUpdateUseCase;
import io.point3.p3api.gallery.domain.entity.StoreGalleryItem;
import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.domain.entity.Store;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GalleryItemService
    implements GalleryItemCreateUseCase,
        GalleryItemQueryUseCase,
        GalleryItemUpdateUseCase,
        GalleryItemDeleteUseCase {

  private final GalleryItemPersistencePort galleryItemPersistencePort;
  private final AssetPersistencePort assetPersistencePort;
  private final AssetVariantDeliveryService assetVariantDeliveryService;
  private final StorePersistencePort storePersistencePort;

  @Override
  public GalleryItemResult create(CreateGalleryItemCommand command) {
    validateAssetOwnership(command.storeId(), command.assetId());

    StoreGalleryItem item =
        StoreGalleryItem.create(command.storeId(), command.assetId(), command.sortOrder());
    changeFeatured(item, command.featured());

    return toResult(galleryItemPersistencePort.save(item));
  }

  @Override
  @Transactional(readOnly = true)
  public List<GalleryItemResult> getSellerItems(UUID storeId) {
    return toResults(galleryItemPersistencePort.findAllByStoreId(storeId));
  }

  @Override
  @Transactional(readOnly = true)
  public GalleryItemResult getSellerItem(UUID storeId, UUID galleryItemId) {
    return toResult(findItem(storeId, galleryItemId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<GalleryItemResult> getVisibleItems(UUID storeId) {
    return toResults(galleryItemPersistencePort.findVisibleByStoreId(storeId)).stream()
        .filter(GalleryItemResult::hasDeliveryUrl)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public GalleryItemResult getVisibleItem(UUID storeId, UUID galleryItemId) {
    StoreGalleryItem item = findItem(storeId, galleryItemId);
    if (item.getStatus() != StoreGalleryItemStatus.VISIBLE) {
      throw new BaseException(GalleryErrorCode.GALLERY_ITEM_NOT_FOUND);
    }
    GalleryItemResult result = toResult(item);
    if (!result.hasDeliveryUrl()) {
      throw new BaseException(GalleryErrorCode.GALLERY_ITEM_NOT_FOUND);
    }
    return result;
  }

  @Override
  public GalleryItemResult update(UpdateGalleryItemCommand command) {
    StoreGalleryItem item = findItem(command.storeId(), command.galleryItemId());
    item.changeSortOrder(command.sortOrder());
    changeFeatured(item, command.featured());
    changeStatus(item, command.status());
    return toResult(item);
  }

  @Override
  public void delete(UUID storeId, UUID galleryItemId) {
    galleryItemPersistencePort.delete(findItem(storeId, galleryItemId));
  }

  private StoreGalleryItem findItem(UUID storeId, UUID galleryItemId) {
    return galleryItemPersistencePort
        .findByIdAndStoreId(galleryItemId, storeId)
        .orElseThrow(() -> new BaseException(GalleryErrorCode.GALLERY_ITEM_NOT_FOUND));
  }

  private void validateAssetOwnership(UUID storeId, UUID assetId) {
    Asset asset = assetPersistencePort
        .findById(assetId)
        .orElseThrow(() -> new BaseException(GalleryErrorCode.GALLERY_ASSET_NOT_FOUND));
    Store store = storePersistencePort
        .findById(storeId)
        .orElseThrow(() -> new BaseException(GalleryErrorCode.GALLERY_ASSET_NOT_FOUND));
    if (!store.getOwnerUserId().equals(asset.getUploadedBy())) {
      throw new BaseException(GalleryErrorCode.GALLERY_ASSET_NOT_FOUND);
    }
  }

  private List<GalleryItemResult> toResults(List<StoreGalleryItem> items) {
    if (items.isEmpty()) {
      return List.of();
    }

    List<UUID> assetIds =
        items.stream().map(StoreGalleryItem::getAssetId).distinct().toList();
    Map<UUID, AssetVariantDelivery> deliveryByAssetId =
        assetVariantDeliveryService.resolveReadyDeliveries(assetIds);

    return items.stream()
        .map(item -> toResult(item, deliveryByAssetId.get(item.getAssetId())))
        .toList();
  }

  private GalleryItemResult toResult(StoreGalleryItem item) {
    return toResults(List.of(item)).getFirst();
  }

  private GalleryItemResult toResult(StoreGalleryItem item, AssetVariantDelivery delivery) {
    AssetVariantDelivery safeDelivery = delivery == null ? AssetVariantDelivery.empty() : delivery;
    return GalleryItemResult.from(
        item,
        safeDelivery.deliveryUrl(),
        safeDelivery.variants().stream()
            .map(variant -> new GalleryItemResult.Variant(
                variant.type(), variant.deliveryUrl(), variant.width(), variant.height()))
            .toList());
  }

  private void changeFeatured(StoreGalleryItem item, boolean featured) {
    if (featured) {
      item.feature();
      return;
    }
    item.unfeature();
  }

  private void changeStatus(StoreGalleryItem item, StoreGalleryItemStatus status) {
    if (status == StoreGalleryItemStatus.VISIBLE) {
      validateReadyVariant(item.getAssetId());
      item.show();
      return;
    }
    item.hide();
  }

  private void validateReadyVariant(UUID assetId) {
    assetVariantDeliveryService.validateReadyDelivery(assetId);
  }
}
