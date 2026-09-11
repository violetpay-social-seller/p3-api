package io.point3.p3api.account.infrastructure.persistence;

import io.point3.p3api.account.application.settlement.port.SellerSettlementAccountPersistencePort;
import io.point3.p3api.account.domain.entity.SellerSettlementAccount;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class SellerSettlementAccountPersistenceAdapter
    implements SellerSettlementAccountPersistencePort {

  private final SellerSettlementAccountJpaRepository repository;

  @Override
  @Transactional(readOnly = true)
  public Optional<SellerSettlementAccount> findByStoreId(UUID storeId) {
    return repository.findByStoreId(storeId);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByStoreId(UUID storeId) {
    return repository.existsByStoreId(storeId);
  }

  @Override
  public SellerSettlementAccount save(SellerSettlementAccount account) {
    return repository.save(account);
  }
}
