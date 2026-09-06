package io.point3.p3api.store.application.refundpolicy.query;

import io.point3.p3api.store.application.refundpolicy.result.StoreRefundPolicyResult;
import java.util.UUID;

public interface StoreRefundPolicyQueryUseCase {

  StoreRefundPolicyResult getRefundPolicy(UUID storeId);
}
