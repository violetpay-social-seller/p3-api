package io.point3.p3api.store.application;

import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.application.businesshours.StoreBusinessHoursTextFormatter;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.create.StoreCreateUseCase;
import io.point3.p3api.store.application.delete.StoreDeleteUseCase;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.application.query.StoreQueryUseCase;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.store.application.setting.port.StoreWeeklyPickupSettingPersistencePort;
import io.point3.p3api.store.application.slug.StoreSlugGenerator;
import io.point3.p3api.store.application.update.ChangeStoreStatusCommand;
import io.point3.p3api.store.application.update.CompleteAccountRegistrationCommand;
import io.point3.p3api.store.application.update.StoreUpdateUseCase;
import io.point3.p3api.store.application.update.UpdateStoreCommand;
import io.point3.p3api.store.application.update.UpdateStoreDescriptionCommand;
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
  private final AssetPersistencePort assetPersistencePort;
  private final StoreActivationValidator storeActivationValidator;
  private final StoreWeeklyPickupSettingPersistencePort weeklyPickupSettingPersistencePort;
  private final StoreBusinessHoursTextFormatter businessHoursTextFormatter;

  @Override
  public StoreResult create(CreateStoreCommand command) {
    if (storePersistencePort.existsByOwnerUserId(command.ownerUserId())) {
      throw new BaseException(StoreErrorCode.STORE_ALREADY_EXISTS);
    }

    Store store = Store.create(command.ownerUserId(), command.name(), generateSlug(command.name()));
    validateProfileAsset(command.profileAssetId(), command.ownerUserId());
    store.updateProfileAsset(command.profileAssetId());
    store.updateBasicInfo(
        command.name(),
        command.description(),
        command.contact(),
        command.contactVisible(),
        command.snsLinks());
    store.initializeLocation(command.address(), command.detailAddress());
    store.updatePickupSettings(command.pickupSettings());

    return toResult(storePersistencePort.save(store));
  }

  @Override
  @Transactional(readOnly = true)
  public StoreResult getStore(UUID storeId) {
    return toResult(findStore(storeId));
  }

  @Override
  public StoreResult update(UpdateStoreCommand command) {
    Store store = findStore(command.storeId());

    validateProfileAsset(command.profileAssetId(), store.getOwnerUserId());
    store.updateProfileAsset(command.profileAssetId());
    store.updateBasicInfo(
        command.name(),
        command.description(),
        command.contact(),
        command.contactVisible(),
        command.snsLinks());
    store.updatePickupSettings(command.pickupSettings());

    return toResult(store);
  }

  @Override
  public StoreResult updateDescription(UpdateStoreDescriptionCommand command) {
    Store store = findStore(command.storeId());
    store.updateDescription(command.description());
    return toResult(store);
  }

  @Override
  public StoreResult completeAccountRegistration(CompleteAccountRegistrationCommand command) {
    Store store = findStore(command.storeId());
    store.markSettlementAccountInputCompleted(java.time.Instant.now());
    return toResult(store);
  }

  @Override
  public StoreResult changeStatus(ChangeStoreStatusCommand command) {
    Store store = findStore(command.storeId());

    if (command.status() == StoreStatus.ACTIVE) {
      validateCanActive(store);
      store.active();
      return toResult(store);
    }

    if (command.status() == StoreStatus.INACTIVE) {
      store.inactive();
      return toResult(store);
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

  private StoreResult toResult(Store store) {
    return StoreResult.from(
        store,
        businessHoursTextFormatter.format(
            weeklyPickupSettingPersistencePort.findAllByStoreId(store.getId())));
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
    storeActivationValidator.validate(store);
  }

  private void validateProfileAsset(UUID profileAssetId, UUID ownerUserId) {
    if (profileAssetId == null) {
      return;
    }

    if (assetPersistencePort.findByIdAndUploadedBy(profileAssetId, ownerUserId).isEmpty()) {
      throw new BaseException(StoreErrorCode.PROFILE_ASSET_NOT_FOUND);
    }
  }
}
