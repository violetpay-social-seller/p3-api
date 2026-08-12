package io.point3.p3api.common.tenant.context;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 *  결정된 storeId를 요청 스코프내에서 꺼내쓰는 역할
 */
@NoArgsConstructor
public final class StoreContext {

  private static final ScopedValue<UUID> STORE_ID = ScopedValue.newInstance();

  public static ScopedValue.Carrier where(UUID storeId) {
    if (storeId == null) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }

    return ScopedValue.where(STORE_ID, storeId);
  }

  public static UUID getStoreId() {
    return STORE_ID.isBound() ? STORE_ID.get() : null;
  }

  public static UUID getRequiredStoreId() {
    UUID storeId = getStoreId();

    if (storeId == null) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }

    return storeId;
  }

  public static boolean hasStore() {
    return STORE_ID.isBound() && STORE_ID.get() != null;
  }
}
