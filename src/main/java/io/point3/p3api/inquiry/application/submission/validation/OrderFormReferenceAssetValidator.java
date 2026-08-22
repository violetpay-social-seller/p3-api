package io.point3.p3api.inquiry.application.submission.validation;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.GalleryErrorCode;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.gallery.application.port.GalleryItemPersistencePort;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderFormReferenceAssetValidator {

  private static final int MAX_REFERENCE_ASSET_COUNT = 5;

  private final GalleryItemPersistencePort galleryItemPersistencePort;

  public void validate(
      UUID storeId, List<CreateOrderFormSubmissionCommand.ReferenceAsset> referenceAssets) {
    if (referenceAssets == null || referenceAssets.isEmpty()) {
      return;
    }

    if (referenceAssets.size() > MAX_REFERENCE_ASSET_COUNT) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_IMAGE_COUNT_EXCEEDED);
    }

    HashSet<UUID> assetIds = new HashSet<>();
    for (CreateOrderFormSubmissionCommand.ReferenceAsset referenceAsset : referenceAssets) {
      validateCommon(referenceAsset, assetIds);
      if (referenceAsset.source() == OrderFormReferenceAssetSource.STORE_GALLERY) {
        validateStoreGalleryAsset(storeId, referenceAsset.assetId());
      }
    }
  }

  private void validateCommon(
      CreateOrderFormSubmissionCommand.ReferenceAsset referenceAsset, HashSet<UUID> assetIds) {
    if (referenceAsset.assetId() == null
        || referenceAsset.source() == null
        || referenceAsset.sortOrder() < 0
        || !assetIds.add(referenceAsset.assetId())) {
      throwInvalidReferenceAsset();
    }
  }

  private void validateStoreGalleryAsset(UUID storeId, UUID assetId) {
    galleryItemPersistencePort
        .findVisibleByAssetIdAndStoreId(assetId, storeId)
        .orElseThrow(() -> new BaseException(GalleryErrorCode.GALLERY_ASSET_NOT_FOUND));
  }

  private void throwInvalidReferenceAsset() {
    throw new BaseException(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID);
  }
}
