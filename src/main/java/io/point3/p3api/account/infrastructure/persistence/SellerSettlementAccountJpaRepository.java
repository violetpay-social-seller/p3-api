package io.point3.p3api.account.infrastructure.persistence;

import io.point3.p3api.account.domain.entity.SellerSettlementAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerSettlementAccountJpaRepository
    extends JpaRepository<SellerSettlementAccount, UUID> {

  Optional<SellerSettlementAccount> findByStoreId(UUID storeId);

  boolean existsByStoreId(UUID storeId);
}
