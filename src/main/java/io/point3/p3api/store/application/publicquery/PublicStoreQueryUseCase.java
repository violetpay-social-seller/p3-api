package io.point3.p3api.store.application.publicquery;

import io.point3.p3api.store.application.publicquery.result.PublicStorePage;
import io.point3.p3api.store.application.publicquery.result.PublicStoreResult;
import java.util.UUID;

public interface PublicStoreQueryUseCase {

  PublicStorePage getStores(PublicStoreListQuery query);

  PublicStoreResult getStore(UUID storeId);
}
