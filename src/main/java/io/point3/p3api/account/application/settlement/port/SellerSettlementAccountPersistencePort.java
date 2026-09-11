package io.point3.p3api.account.application.settlement.port;

import io.point3.p3api.account.domain.entity.SellerSettlementAccount;
import java.util.Optional;
import java.util.UUID;

public interface SellerSettlementAccountPersistencePort {

  Optional<SellerSettlementAccount> findByStoreId(UUID storeId);

  boolean existsByStoreId(UUID storeId);

  SellerSettlementAccount save(SellerSettlementAccount account);
}
