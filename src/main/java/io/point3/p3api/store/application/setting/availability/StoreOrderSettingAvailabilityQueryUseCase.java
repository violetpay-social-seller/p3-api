package io.point3.p3api.store.application.setting.availability;

import io.point3.p3api.store.application.setting.availability.result.StoreOrderSettingAvailabilityResult;
import java.time.LocalDate;
import java.util.UUID;

public interface StoreOrderSettingAvailabilityQueryUseCase {

  StoreOrderSettingAvailabilityResult getAvailability(UUID storeId, LocalDate from, LocalDate to);
}
