package io.point3.p3api.store.application.setting.query;

import io.point3.p3api.store.application.setting.result.StoreSettingResult;
import java.util.UUID;

public interface StoreSettingQueryUseCase {

  StoreSettingResult getSetting(UUID storeId);
}
