package io.point3.p3api.store.application.management;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.gallery.application.port.GalleryItemPersistencePort;
import io.point3.p3api.orderform.application.port.OrderFormPersistencePort;
import io.point3.p3api.store.application.management.result.StoreManagementStatusResult;
import io.point3.p3api.store.application.notice.port.StoreNoticePersistencePort;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.application.representative.port.RepresentativeImagePersistencePort;
import io.point3.p3api.store.application.setting.port.StoreWeeklyPickupSettingPersistencePort;
import io.point3.p3api.store.domain.entity.Store;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreManagementStatusQueryService implements StoreManagementStatusQueryUseCase {

  private final StorePersistencePort storePersistencePort;
  private final OrderFormPersistencePort orderFormPersistencePort;
  private final StoreNoticePersistencePort storeNoticePersistencePort;
  private final StoreWeeklyPickupSettingPersistencePort weeklyPickupSettingPersistencePort;
  private final GalleryItemPersistencePort galleryItemPersistencePort;
  private final RepresentativeImagePersistencePort representativeImagePersistencePort;

  @Override
  public StoreManagementStatusResult getStatus(UUID storeId) {
    Store store = storePersistencePort
        .findById(storeId)
        .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
    boolean orderForm = orderFormPersistencePort.existsActiveTemplateByStoreId(storeId);
    boolean notice = storeNoticePersistencePort.hasCompleteNotices(storeId);
    boolean galleryReady =
        !galleryItemPersistencePort.findVisibleByStoreId(storeId).isEmpty();
    boolean representativeReady =
        representativeImagePersistencePort.findActiveByStoreId(storeId).size() >= 3;
    boolean photoRegistration = galleryReady && representativeReady;
    boolean settlementAccount = "INPUT_COMPLETED".equals(store.getSettlementAccountStatus());
    boolean storeInfo = hasText(store.getAddress())
        && hasText(store.getBusinessHours())
        && hasText(store.getCancellationRefundPolicy());
    List<String> reasons = blockedReasons(
        orderForm, notice, galleryReady, representativeReady, settlementAccount, storeId);
    int completedCount = (storeInfo ? 1 : 0)
        + (orderForm ? 1 : 0)
        + (notice ? 1 : 0)
        + (photoRegistration ? 1 : 0)
        + (settlementAccount ? 1 : 0);
    return new StoreManagementStatusResult(
        store.getName(),
        completedCount,
        5,
        new StoreManagementStatusResult.Items(
            storeInfo, orderForm, notice, photoRegistration, settlementAccount),
        reasons.isEmpty(),
        List.copyOf(reasons));
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  private List<String> blockedReasons(
      boolean orderForm,
      boolean notice,
      boolean galleryReady,
      boolean representativeReady,
      boolean settlementAccount,
      UUID storeId) {
    List<String> reasons = new ArrayList<>();
    if (!orderForm) {
      reasons.add("ACTIVE_ORDER_FORM_REQUIRED");
    }
    if (!notice) {
      reasons.add("ORDER_NOTICE_REQUIRED");
    }
    if (weeklyPickupSettingPersistencePort.findAllByStoreId(storeId).stream()
        .noneMatch(setting -> setting.isEnabled())) {
      reasons.add("ENABLED_PICKUP_SETTING_REQUIRED");
    }
    if (!galleryReady) {
      reasons.add("GALLERY_IMAGE_REQUIRED");
    }
    if (!representativeReady) {
      reasons.add("REPRESENTATIVE_IMAGES_REQUIRED");
    }
    if (!settlementAccount) {
      reasons.add("SETTLEMENT_ACCOUNT_REQUIRED");
    }
    return reasons;
  }
}
