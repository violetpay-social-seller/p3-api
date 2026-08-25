package io.point3.p3api.store.application.setting.update;

import io.point3.p3api.store.application.setting.command.UpdateStoreSettingCommand;
import io.point3.p3api.store.application.setting.result.StoreSettingResult;

public interface StoreSettingUpdateUseCase {

  StoreSettingResult update(UpdateStoreSettingCommand command);
}
