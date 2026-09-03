package io.point3.p3api.store.controller.response;

import io.point3.p3api.store.application.management.result.StoreManagementStatusResult;
import java.util.List;

public record StoreManagementStatusResponse(
    String storeName,
    int completedCount,
    int totalCount,
    Items items,
    boolean canActivate,
    List<String> activationBlockedReasons) {

  public StoreManagementStatusResponse {
    activationBlockedReasons = List.copyOf(activationBlockedReasons);
  }

  public static StoreManagementStatusResponse from(StoreManagementStatusResult result) {
    return new StoreManagementStatusResponse(
        result.storeName(),
        result.completedCount(),
        result.totalCount(),
        Items.from(result.items()),
        result.canActivate(),
        result.activationBlockedReasons());
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
      boolean settlementAccount) {

    private static Items from(StoreManagementStatusResult.Items items) {
      return new Items(
          items.storeInfo(),
          items.orderForm(),
          items.notice(),
          items.photoRegistration(),
          items.settlementAccount());
    }
  }
}
