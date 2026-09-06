package io.point3.p3api.store.application.businesshours.query;

import io.point3.p3api.store.application.businesshours.result.StoreBusinessHoursResult;
import java.util.UUID;

public interface StoreBusinessHoursQueryUseCase {

  StoreBusinessHoursResult getBusinessHours(UUID storeId);
}
