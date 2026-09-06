package io.point3.p3api.store.application.refundpolicy.port;

import io.point3.p3api.store.domain.entity.StoreRefundPolicy;
import java.util.List;
import java.util.UUID;

public interface StoreRefundPolicyPersistencePort {

  List<StoreRefundPolicy> findAllByStoreId(UUID storeId);

  List<StoreRefundPolicy> replaceAllByStoreId(UUID storeId, List<StoreRefundPolicy> policies);
}
