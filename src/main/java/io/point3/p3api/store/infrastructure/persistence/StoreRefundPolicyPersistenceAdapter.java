package io.point3.p3api.store.infrastructure.persistence;

import io.point3.p3api.store.application.refundpolicy.port.StoreRefundPolicyPersistencePort;
import io.point3.p3api.store.domain.entity.StoreRefundPolicy;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class StoreRefundPolicyPersistenceAdapter implements StoreRefundPolicyPersistencePort {

  private final StoreRefundPolicyJpaRepository storeRefundPolicyJpaRepository;

  @Override
  @Transactional(readOnly = true)
  public List<StoreRefundPolicy> findAllByStoreId(UUID storeId) {
    return storeRefundPolicyJpaRepository.findAllByStoreIdOrderBySortOrderAsc(storeId);
  }

  @Override
  public List<StoreRefundPolicy> replaceAllByStoreId(
      UUID storeId, List<StoreRefundPolicy> policies) {
    storeRefundPolicyJpaRepository.deleteAllByStoreId(storeId);
    storeRefundPolicyJpaRepository.flush();
    return storeRefundPolicyJpaRepository.saveAll(policies);
  }
}
