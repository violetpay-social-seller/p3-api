package io.point3.p3api.store.application;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.create.StoreCreateUseCase;
import io.point3.p3api.store.application.delete.StoreDeleteUseCase;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.application.query.StoreQueryUseCase;
import io.point3.p3api.store.application.representative.port.RepresentativeImagePersistencePort;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.store.application.slug.StoreSlugGenerator;
import io.point3.p3api.store.application.update.ChangeStoreStatusCommand;
import io.point3.p3api.store.application.update.StoreUpdateUseCase;
import io.point3.p3api.store.application.update.UpdateStoreCommand;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.store.domain.type.StoreStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StoreService
    implements StoreCreateUseCase, StoreQueryUseCase, StoreUpdateUseCase, StoreDeleteUseCase {

  private static final int MAX_SLUG_SUFFIX_ATTEMPTS = 100;

  private final StorePersistencePort storePersistencePort;
  private final RepresentativeImagePersistencePort representativeImagePersistencePort;
  private final StoreActivationValidator storeActivationValidator;

  @Override
  public StoreResult create(CreateStoreCommand command) {
    if (storePersistencePort.existsByOwnerUserId(command.ownerUserId())) {
      throw new BaseException(StoreErrorCode.STORE_ALREADY_EXISTS);
    }

    Store store = Store.create(command.ownerUserId(), command.name(), generateSlug(command.name()));
    store.updateProfileAsset(command.profileAssetId());
    store.updateBasicInfo(
        command.name(),
        command.description(),
        command.contact(),
        command.contactVisible(),
        command.snsLinks(),
        command.businessHours(),
        command.address());
    store.updatePickupSettings(command.pickupSettings());

    return StoreResult.from(storePersistencePort.save(store));
  }

  @Override
  @Transactional(readOnly = true)
  public StoreResult getStore(UUID storeId) {
    return StoreResult.from(findStore(storeId));
  }

  @Override
  public StoreResult update(UpdateStoreCommand command) {
    Store store = findStore(command.storeId());

    store.updateProfileAsset(command.profileAssetId());
    store.updateBasicInfo(
        command.name(),
        command.description(),
        command.contact(),
        command.contactVisible(),
        command.snsLinks(),
        command.businessHours(),
        command.address());
    store.updatePickupSettings(command.pickupSettings());

    return StoreResult.from(store);
  }

  @Override
  public StoreResult changeStatus(ChangeStoreStatusCommand command) {
    Store store = findStore(command.storeId());

    if (command.status() == StoreStatus.ACTIVE) {
      validateCanActive(store);
      store.active();
      return StoreResult.from(store);
    }

    if (command.status() == StoreStatus.INACTIVE) {
      store.inactive();
      return StoreResult.from(store);
    }

    throw new BaseException(StoreErrorCode.STORE_STATUS_FORBIDDEN);
  }

  @Override
  public void delete(UUID storeId) {
    Store store = findStore(storeId);
    store.delete();
  }

  private Store findStore(UUID storeId) {
    return storePersistencePort
        .findById(storeId)
        .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
  }

  private Store getStoreByOwner(UUID ownerUserId) {
    return storePersistencePort
        .findByOwnerUserId(ownerUserId)
        .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
  }

  private String generateSlug(String storeName) {
    String baseSlug = StoreSlugGenerator.base(storeName);

    if (!storePersistencePort.existsBySlug(baseSlug)) {
      return baseSlug;
    }

    for (int suffix = 1; suffix <= MAX_SLUG_SUFFIX_ATTEMPTS; suffix++) {
      String candidate = baseSlug + "-" + suffix;
      if (!storePersistencePort.existsBySlug(candidate)) {
        return candidate;
      }
    }

    throw new BaseException(StoreErrorCode.STORE_ALREADY_EXISTS);
  }

  private void validateCanActive(Store store) {
    validateRepresentativeImageReady(store.getId());
    storeActivationValidator.validate(store);
  }

  private void validateRepresentativeImageReady(UUID storeId) {
    if (representativeImagePersistencePort.findActiveByStoreId(storeId).size() < 3) {
      throw new BaseException(StoreErrorCode.REPRESENTATIVE_IMAGE_MINIMUM_REQUIRED);
    }
  }
}
