package io.point3.p3api.store.infrastructure.persistence;

import io.point3.p3api.store.application.setting.port.StoreWeeklyPickupSettingPersistencePort;
import io.point3.p3api.store.domain.entity.StoreWeeklyPickupSetting;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class StoreWeeklyPickupSettingPersistenceAdapter
    implements StoreWeeklyPickupSettingPersistencePort {

  private final StoreWeeklyPickupSettingJpaRepository storeWeeklyPickupSettingJpaRepository;

  @Override
  public List<StoreWeeklyPickupSetting> saveAll(List<StoreWeeklyPickupSetting> settings) {
    return storeWeeklyPickupSettingJpaRepository.saveAll(settings);
  }

  @Override
  public List<StoreWeeklyPickupSetting> findAllByStoreId(UUID storeId) {
    return storeWeeklyPickupSettingJpaRepository.findAllByStoreIdOrderByDayOfWeekAsc(storeId);
  }

  @Override
  public void deleteAllByStoreId(UUID storeId) {
    storeWeeklyPickupSettingJpaRepository.deleteAllByStoreId(storeId);
    storeWeeklyPickupSettingJpaRepository.flush();
  }
}
