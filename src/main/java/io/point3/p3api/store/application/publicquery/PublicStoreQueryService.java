package io.point3.p3api.store.application.publicquery;

import io.point3.p3api.asset.application.AssetDeliveryUrlResolver;
import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.asset.domain.type.AssetStatus;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.application.publicquery.result.PublicRepresentativeImageResult;
import io.point3.p3api.store.application.publicquery.result.PublicStorePage;
import io.point3.p3api.store.application.publicquery.result.PublicStoreResult;
import io.point3.p3api.store.application.representative.port.RepresentativeImagePersistencePort;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.store.domain.entity.StoreRepresentativeImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicStoreQueryService implements PublicStoreQueryUseCase {

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 100;

  private final StorePersistencePort storePersistencePort;
  private final RepresentativeImagePersistencePort representativeImagePersistencePort;
  private final AssetPersistencePort assetPersistencePort;
  private final AssetDeliveryUrlResolver assetDeliveryUrlResolver;

  @Override
  public PublicStorePage getStores(PublicStoreListQuery query) {
    validateCursor(query);
    int size = resolvePageSize(query.size());
    List<Store> stores =
        storePersistencePort.findActiveStores(query.cursorUpdatedAt(), query.cursorId(), size + 1);
    boolean hasNext = stores.size() > size;
    List<Store> pageStores = hasNext ? stores.subList(0, size) : stores;
    Store lastStore = hasNext ? pageStores.getLast() : null;

    return new PublicStorePage(
        pageStores.stream().map(this::toResult).toList(),
        hasNext,
        lastStore == null ? null : lastStore.getUpdatedAt(),
        lastStore == null ? null : lastStore.getId());
  }

  @Override
  public PublicStoreResult getStore(UUID storeId) {
    Store store = storePersistencePort
        .findById(storeId)
        .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
    if (!store.isActive()) {
      throw new BaseException(StoreErrorCode.STORE_NOT_FOUND);
    }
    return toResult(store);
  }

  private PublicStoreResult toResult(Store store) {
    List<StoreRepresentativeImage> representativeImages =
        representativeImagePersistencePort.findActiveByStoreId(store.getId());
    Map<UUID, Asset> assetsById = findAssets(store.getProfileAssetId(), representativeImages);

    Asset profileAsset = assetsById.get(store.getProfileAssetId());
    List<PublicRepresentativeImageResult> publicImages = representativeImages.stream()
        .map(image -> toPublicImage(image, assetsById.get(image.getAssetId())))
        .filter(java.util.Objects::nonNull)
        .toList();

    return new PublicStoreResult(
        store.getId(),
        store.getProfileAssetId(),
        deliveryUrl(profileAsset),
        store.getName(),
        store.getSlug(),
        store.getDescription(),
        store.isContactVisible() ? store.getContact() : null,
        store.isContactVisible(),
        store.getSnsLinks(),
        store.getBusinessHours(),
        store.getAddress(),
        publicImages);
  }

  private Map<UUID, Asset> findAssets(
      UUID profileAssetId, List<StoreRepresentativeImage> representativeImages) {
    List<UUID> assetIds = java.util.stream.Stream.concat(
            java.util.stream.Stream.of(profileAssetId),
            representativeImages.stream().map(StoreRepresentativeImage::getAssetId))
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();
    Map<UUID, Asset> assetsById = new HashMap<>();
    assetPersistencePort
        .findAllById(assetIds)
        .forEach(asset -> assetsById.put(asset.getId(), asset));
    return assetsById;
  }

  private PublicRepresentativeImageResult toPublicImage(
      StoreRepresentativeImage image, Asset asset) {
    if (asset == null || asset.getStatus() == AssetStatus.DELETED) {
      return null;
    }
    return new PublicRepresentativeImageResult(
        image.getId(), image.getAssetId(), deliveryUrl(asset), image.getSortOrder());
  }

  private String deliveryUrl(Asset asset) {
    if (asset == null || asset.getStatus() == AssetStatus.DELETED) {
      return null;
    }
    return assetDeliveryUrlResolver.resolve(asset.getObjectKey());
  }

  private void validateCursor(PublicStoreListQuery query) {
    if ((query.cursorUpdatedAt() == null) != (query.cursorId() == null)) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }
  }

  private int resolvePageSize(Integer requestedSize) {
    int size = requestedSize == null ? DEFAULT_PAGE_SIZE : requestedSize;
    if (size < 1 || size > MAX_PAGE_SIZE) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }
    return size;
  }
}
