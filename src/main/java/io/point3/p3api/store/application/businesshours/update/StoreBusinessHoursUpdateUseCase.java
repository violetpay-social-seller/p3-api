package io.point3.p3api.store.application.businesshours.update;

import io.point3.p3api.store.application.businesshours.command.UpdateStoreBusinessHoursCommand;
import io.point3.p3api.store.application.businesshours.result.StoreBusinessHoursResult;

public interface StoreBusinessHoursUpdateUseCase {

  StoreBusinessHoursResult updateBusinessHours(UpdateStoreBusinessHoursCommand command);
}
