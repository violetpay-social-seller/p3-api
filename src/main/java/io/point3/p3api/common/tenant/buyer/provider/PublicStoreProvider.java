package io.point3.p3api.common.tenant.buyer.provider;

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
public class PublicStoreProvider {

  private final StorePersistencePort storePersistencePort;

  @Transactional(readOnly = true)
  public Store resolveStore(String slug) {
    Store store = storePersistencePort
        .findBySlug(slug)
        .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));

    if (!store.isActive()) {
      throw new BaseException(StoreErrorCode.STORE_NOT_FOUND);
    }

    return store;
  }

  public UUID resolveStoreId(String slug) {
    return resolveStore(slug).getId();
  }
}
