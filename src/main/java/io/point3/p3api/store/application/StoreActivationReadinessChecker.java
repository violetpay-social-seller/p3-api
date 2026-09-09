package io.point3.p3api.store.application;

import io.point3.p3api.orderform.application.port.OrderFormPersistencePort;
import io.point3.p3api.store.application.notice.port.StoreNoticePersistencePort;
import io.point3.p3api.store.application.representative.port.RepresentativeImagePersistencePort;
import io.point3.p3api.store.application.setting.port.StoreWeeklyPickupSettingPersistencePort;
import io.point3.p3api.store.domain.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreActivationReadinessChecker {

  private static final int MINIMUM_REPRESENTATIVE_IMAGE_COUNT = 3;
  private static final String SETTLEMENT_ACCOUNT_INPUT_COMPLETED = "INPUT_COMPLETED";

  private final OrderFormPersistencePort orderFormPersistencePort;
  private final StoreNoticePersistencePort storeNoticePersistencePort;
  private final StoreWeeklyPickupSettingPersistencePort weeklyPickupSettingPersistencePort;
  private final RepresentativeImagePersistencePort representativeImagePersistencePort;

  public StoreActivationReadiness check(Store store) {
    boolean enabledPickupSetting =
        weeklyPickupSettingPersistencePort.findAllByStoreId(store.getId()).stream()
            .anyMatch(setting -> setting.isEnabled());
    boolean storeInfo = hasText(store.getDescription())
        && hasText(store.getAddress())
        && hasText(store.getCancellationRefundPolicy())
        && enabledPickupSetting;

    return new StoreActivationReadiness(
        storeInfo,
        orderFormPersistencePort.existsActiveTemplateByStoreId(store.getId()),
        storeNoticePersistencePort.hasCompleteNotices(store.getId()),
        representativeImagePersistencePort.findActiveByStoreId(store.getId()).size()
            >= MINIMUM_REPRESENTATIVE_IMAGE_COUNT,
        SETTLEMENT_ACCOUNT_INPUT_COMPLETED.equals(store.getSettlementAccountStatus()));
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
