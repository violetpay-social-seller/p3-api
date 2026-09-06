package io.point3.p3api.store.infrastructure.persistence;

import io.point3.p3api.store.domain.entity.StoreRefundPolicy;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRefundPolicyJpaRepository extends JpaRepository<StoreRefundPolicy, UUID> {

  List<StoreRefundPolicy> findAllByStoreIdOrderBySortOrderAsc(UUID storeId);

  void deleteAllByStoreId(UUID storeId);
}
