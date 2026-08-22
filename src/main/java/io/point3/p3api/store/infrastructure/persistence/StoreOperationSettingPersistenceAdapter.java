package io.point3.p3api.store.infrastructure.persistence;

import io.point3.p3api.store.application.setting.port.StoreOperationSettingPersistencePort;
import io.point3.p3api.store.domain.entity.StoreOperationSetting;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class StoreOperationSettingPersistenceAdapter
    implements StoreOperationSettingPersistencePort {

  private final StoreOperationSettingJpaRepository storeOperationSettingJpaRepository;

  @Override
  public StoreOperationSetting save(StoreOperationSetting setting) {
    return storeOperationSettingJpaRepository.save(setting);
  }

  @Override
  public Optional<StoreOperationSetting> findByStoreId(UUID storeId) {
    return storeOperationSettingJpaRepository.findById(storeId);
  }
}
