package io.point3.p3api.store.application.refundpolicy.update;

import io.point3.p3api.store.application.refundpolicy.command.UpdateStoreRefundPolicyCommand;
import io.point3.p3api.store.application.refundpolicy.result.StoreRefundPolicyResult;

public interface StoreRefundPolicyUpdateUseCase {

  StoreRefundPolicyResult updateRefundPolicy(UpdateStoreRefundPolicyCommand command);
}
