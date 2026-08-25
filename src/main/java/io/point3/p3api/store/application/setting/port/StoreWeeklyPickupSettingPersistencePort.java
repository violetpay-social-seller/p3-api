package io.point3.p3api.store.application.setting.port;

import io.point3.p3api.store.domain.entity.StoreWeeklyPickupSetting;
import java.util.List;
import java.util.UUID;

public interface StoreWeeklyPickupSettingPersistencePort {

  List<StoreWeeklyPickupSetting> saveAll(List<StoreWeeklyPickupSetting> settings);

  List<StoreWeeklyPickupSetting> findAllByStoreId(UUID storeId);

  void deleteAllByStoreId(UUID storeId);
}
