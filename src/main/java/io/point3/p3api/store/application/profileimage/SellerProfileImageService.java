package io.point3.p3api.store.application.profileimage;

import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.user.application.port.UserPersistencePort;
import io.point3.p3api.user.application.profile.ProfileImageDeliveryService;
import io.point3.p3api.user.domain.entity.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerProfileImageService implements SellerProfileImageUpdateUseCase {

  private final StorePersistencePort storePersistencePort;
  private final UserPersistencePort userPersistencePort;
  private final AssetPersistencePort assetPersistencePort;
  private final ProfileImageDeliveryService profileImageDeliveryService;

  @Override
  @Transactional
  public SellerProfileImageResult update(UpdateSellerProfileImageCommand command) {
    Store store = storePersistencePort
        .findById(command.storeId())
        .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
    User user = userPersistencePort
        .findById(command.userId())
        .orElseThrow(() -> new BaseException(CommonErrorCode.UNAUTHORIZED));

    if (!store.getOwnerUserId().equals(user.getId())) {
      throw new BaseException(CommonErrorCode.UNAUTHORIZED);
    }

    validateOwnedAsset(command.profileAssetId(), user.getId());
    store.updateProfileAsset(command.profileAssetId());
    user.updateProfileAsset(command.profileAssetId());

    return new SellerProfileImageResult(
        command.profileAssetId(), profileImageDeliveryService.resolve(user));
  }

  private void validateOwnedAsset(UUID profileAssetId, UUID userId) {
    if (profileAssetId == null) {
      return;
    }

    if (assetPersistencePort.findByIdAndUploadedBy(profileAssetId, userId).isEmpty()) {
      throw new BaseException(StoreErrorCode.PROFILE_ASSET_NOT_FOUND);
    }
  }
}
