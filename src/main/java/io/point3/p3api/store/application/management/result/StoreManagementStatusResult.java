package io.point3.p3api.store.application.management.result;

import java.util.List;

public record StoreManagementStatusResult(
    String storeName,
    int completedCount,
    int totalCount,
    Items items,
    boolean canActivate,
    List<String> activationBlockedReasons) {

  public StoreManagementStatusResult {
    activationBlockedReasons = List.copyOf(activationBlockedReasons);
  }

  @Override
  public List<String> activationBlockedReasons() {
    return List.copyOf(activationBlockedReasons);
  }

  public record Items(
      boolean storeInfo,
      boolean orderForm,
      boolean notice,
      boolean photoRegistration,
      boolean settlementAccount) {}
}
