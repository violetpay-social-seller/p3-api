package io.point3.p3api.store.application.setting.port;

import io.point3.p3api.store.domain.entity.StoreOperationSetting;
import java.util.Optional;
import java.util.UUID;

public interface StoreOperationSettingPersistencePort {

  StoreOperationSetting save(StoreOperationSetting setting);

  Optional<StoreOperationSetting> findByStoreId(UUID storeId);
}
