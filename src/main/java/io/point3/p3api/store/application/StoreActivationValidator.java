package io.point3.p3api.store.application;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.orderform.application.port.OrderFormPersistencePort;
import io.point3.p3api.store.application.notice.port.StoreNoticePersistencePort;
import io.point3.p3api.store.application.setting.port.StoreOperationSettingPersistencePort;
import io.point3.p3api.store.application.setting.port.StoreWeeklyPickupSettingPersistencePort;
import io.point3.p3api.store.domain.entity.Store;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreActivationValidator {

  private static final String SETTLEMENT_ACCOUNT_INPUT_COMPLETED = "INPUT_COMPLETED";

  private final OrderFormPersistencePort orderFormPersistencePort;
  private final StoreNoticePersistencePort storeNoticePersistencePort;
  private final StoreOperationSettingPersistencePort storeOperationSettingPersistencePort;
  private final StoreWeeklyPickupSettingPersistencePort weeklyPickupSettingPersistencePort;

  public void validate(Store store) {
    UUID storeId = store.getId();
    validateActiveOrderForm(storeId);
    requireOperationSetting(storeId);
    validateOrderNotice(storeId);
    validateEnabledPickupSetting(storeId);
    validateSettlementAccount(store);
  }

  private void validateActiveOrderForm(UUID storeId) {
    if (!orderFormPersistencePort.existsActiveTemplateByStoreId(storeId)) {
      throw new BaseException(StoreErrorCode.ACTIVE_ORDER_FORM_REQUIRED);
    }
  }

  private void requireOperationSetting(UUID storeId) {
    storeOperationSettingPersistencePort
        .findByStoreId(storeId)
        .orElseThrow(() -> new BaseException(StoreErrorCode.OPERATION_SETTING_REQUIRED));
  }

  private void validateOrderNotice(UUID storeId) {
    if (!storeNoticePersistencePort.hasCompleteNotices(storeId)) {
      throw new BaseException(StoreErrorCode.ORDER_NOTICE_REQUIRED);
    }
  }

  private void validateEnabledPickupSetting(UUID storeId) {
    boolean hasEnabledPickupSetting =
        weeklyPickupSettingPersistencePort.findAllByStoreId(storeId).stream()
            .anyMatch(setting -> setting.isEnabled());
    if (!hasEnabledPickupSetting) {
      throw new BaseException(StoreErrorCode.ENABLED_PICKUP_SETTING_REQUIRED);
    }
  }

  private void validateSettlementAccount(Store store) {
    if (!SETTLEMENT_ACCOUNT_INPUT_COMPLETED.equals(store.getSettlementAccountStatus())) {
      throw new BaseException(StoreErrorCode.SETTLEMENT_ACCOUNT_REQUIRED);
    }
  }
}
