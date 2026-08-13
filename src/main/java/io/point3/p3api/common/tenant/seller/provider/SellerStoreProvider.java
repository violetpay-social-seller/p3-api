package io.point3.p3api.common.tenant.seller.provider;

import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.domain.entity.Store;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SellerStoreProvider {

  private final StorePersistencePort storePersistencePort;

  @Transactional(readOnly = true)
  public Store resolveStore(CurrentUser currentUser) {
    return storePersistencePort
        .findByOwnerUserId(currentUser.userId())
        .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public UUID resolveStoreId(CurrentUser currentUser) {
    return resolveStore(currentUser).getId();
  }
}
