package io.point3.p3api.store.application;

import io.point3.p3api.exception.code.StoreErrorCode;
import java.util.ArrayList;
import java.util.List;

public record StoreActivationReadiness(
    boolean storeInfo,
    boolean orderForm,
    boolean notice,
    boolean photoRegistration,
    boolean settlementAccount) {

  public int completedCount() {
    return (storeInfo ? 1 : 0)
        + (orderForm ? 1 : 0)
        + (notice ? 1 : 0)
        + (photoRegistration ? 1 : 0)
        + (settlementAccount ? 1 : 0);
  }

  public boolean canActivate() {
    return completedCount() == 5;
  }

  public List<String> blockedReasons() {
    List<String> reasons = new ArrayList<>();
    if (!storeInfo) {
      reasons.add("STORE_INFORMATION_REQUIRED");
    }
    if (!orderForm) {
      reasons.add("ACTIVE_ORDER_FORM_REQUIRED");
    }
    if (!notice) {
      reasons.add("ORDER_NOTICE_REQUIRED");
    }
    if (!photoRegistration) {
      reasons.add("REPRESENTATIVE_IMAGES_REQUIRED");
    }
    if (!settlementAccount) {
      reasons.add("SETTLEMENT_ACCOUNT_REQUIRED");
    }
    return List.copyOf(reasons);
  }

  public StoreErrorCode firstBlockingError() {
    if (!photoRegistration) {
      return StoreErrorCode.REPRESENTATIVE_IMAGE_MINIMUM_REQUIRED;
    }
    if (!orderForm) {
      return StoreErrorCode.ACTIVE_ORDER_FORM_REQUIRED;
    }
    if (!notice) {
      return StoreErrorCode.ORDER_NOTICE_REQUIRED;
    }
    if (!storeInfo) {
      return StoreErrorCode.STORE_INFORMATION_REQUIRED;
    }
    if (!settlementAccount) {
      return StoreErrorCode.SETTLEMENT_ACCOUNT_REQUIRED;
    }
    return null;
  }
}
