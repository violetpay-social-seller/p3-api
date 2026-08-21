package io.point3.p3api.store.application.representative;

import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.application.representative.command.CreateRepresentativeImageCommand;
import io.point3.p3api.store.application.representative.command.UpdateRepresentativeImageCommand;
import io.point3.p3api.store.application.representative.create.RepresentativeImageCreateUseCase;
import io.point3.p3api.store.application.representative.delete.RepresentativeImageDeleteUseCase;
import io.point3.p3api.store.application.representative.port.RepresentativeImagePersistencePort;
import io.point3.p3api.store.application.representative.query.RepresentativeImageQueryUseCase;
import io.point3.p3api.store.application.representative.result.RepresentativeImageResult;
import io.point3.p3api.store.application.representative.update.RepresentativeImageUpdateUseCase;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.store.domain.entity.StoreRepresentativeImage;
import io.point3.p3api.store.domain.type.StoreRepresentativeImageStatus;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RepresentativeImageService
    implements RepresentativeImageCreateUseCase,
        RepresentativeImageQueryUseCase,
        RepresentativeImageUpdateUseCase,
        RepresentativeImageDeleteUseCase {

  private static final int MAX_IMAGE_COUNT = 10;

  private final RepresentativeImagePersistencePort representativeImagePersistencePort;
  private final AssetPersistencePort assetPersistencePort;
  private final StorePersistencePort storePersistencePort;

  @Override
  public RepresentativeImageResult create(CreateRepresentativeImageCommand command) {
    validateImageLimit(command.storeId());
    validateAssetOwnership(command.storeId(), command.assetId());
    StoreRepresentativeImage image =
        StoreRepresentativeImage.create(command.storeId(), command.assetId(), command.sortOrder());
    return RepresentativeImageResult.from(representativeImagePersistencePort.save(image));
  }

  @Override
  @Transactional(readOnly = true)
  public List<RepresentativeImageResult> getSellerImages(UUID storeId) {
    return representativeImagePersistencePort.findAllByStoreId(storeId).stream()
        .map(RepresentativeImageResult::from)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public RepresentativeImageResult getSellerImage(UUID storeId, UUID imageId) {
    return RepresentativeImageResult.from(findImage(storeId, imageId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<RepresentativeImageResult> getActiveImages(UUID storeId) {
    return representativeImagePersistencePort.findActiveByStoreId(storeId).stream()
        .map(RepresentativeImageResult::from)
        .toList();
  }

  @Override
  public RepresentativeImageResult update(UpdateRepresentativeImageCommand command) {
    Store store = findStore(command.storeId());
    StoreRepresentativeImage image = findImage(command.storeId(), command.representativeImageId());

    image.changeSortOrder(command.sortOrder());
    changeStatus(store, image, command.status());
    return RepresentativeImageResult.from(image);
  }

  @Override
  public void delete(UUID storeId, UUID imageId) {
    Store store = findStore(storeId);
    StoreRepresentativeImage image = findImage(storeId, imageId);

    validateCanRemoveActiveRepresentativeImage(store, image);
    representativeImagePersistencePort.delete(image);
  }

  private Store findStore(UUID storeId) {
    return storePersistencePort
        .findById(storeId)
        .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
  }

  private StoreRepresentativeImage findImage(UUID storeId, UUID imageId) {
    return representativeImagePersistencePort
        .findByIdAndStoreId(imageId, storeId)
        .orElseThrow(() -> new BaseException(StoreErrorCode.REPRESENTATIVE_IMAGE_NOT_FOUND));
  }

  private void validateImageLimit(UUID storeId) {
    if (representativeImagePersistencePort.countByStoreId(storeId) >= MAX_IMAGE_COUNT) {
      throw new BaseException(StoreErrorCode.REPRESENTATIVE_IMAGE_LIMIT_EXCEEDED);
    }
  }

  private void validateAssetOwnership(UUID storeId, UUID assetId) {
    Asset asset = assetPersistencePort
        .findById(assetId)
        .orElseThrow(() -> new BaseException(StoreErrorCode.REPRESENTATIVE_IMAGE_ASSET_NOT_FOUND));
    Store store = storePersistencePort
        .findById(storeId)
        .orElseThrow(() -> new BaseException(StoreErrorCode.REPRESENTATIVE_IMAGE_ASSET_NOT_FOUND));
    if (!store.getOwnerUserId().equals(asset.getUploadedBy())) {
      throw new BaseException(StoreErrorCode.REPRESENTATIVE_IMAGE_ASSET_NOT_FOUND);
    }
  }

  private void changeStatus(
      Store store, StoreRepresentativeImage image, StoreRepresentativeImageStatus status) {
    if (status == StoreRepresentativeImageStatus.ACTIVE) {
      image.show();
      return;
    }

    validateCanRemoveActiveRepresentativeImage(store, image);
    image.hide();
  }

  private void validateCanRemoveActiveRepresentativeImage(
      Store store, StoreRepresentativeImage image) {
    if (!store.isActive()) {
      return;
    }

    if (image.getStatus() != StoreRepresentativeImageStatus.ACTIVE) {
      return;
    }

    if (representativeImagePersistencePort.findActiveByStoreId(store.getId()).size() <= 3) {
      throw new BaseException(StoreErrorCode.REPRESENTATIVE_IMAGE_MINIMUM_REQUIRED);
    }
  }
}
