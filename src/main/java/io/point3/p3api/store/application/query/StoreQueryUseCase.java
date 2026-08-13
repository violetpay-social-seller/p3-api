package io.point3.p3api.store.application.query;

import io.point3.p3api.store.application.result.StoreResult;
import java.util.UUID;

public interface StoreQueryUseCase {

  StoreResult getStore(UUID storeId);
}
