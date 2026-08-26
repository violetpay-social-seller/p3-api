package io.point3.p3api.operator.application.command;

import io.point3.p3api.store.domain.type.StoreStatus;
import java.util.UUID;

public record ChangeStoreStatusCommand(
    UUID storeId, UUID operatorUserId, StoreStatus status, String reason) {

  public static ChangeStoreStatusCommand of(
      UUID storeId, UUID operatorUserId, StoreStatus status, String reason) {
    return new ChangeStoreStatusCommand(storeId, operatorUserId, status, reason);
  }
}
