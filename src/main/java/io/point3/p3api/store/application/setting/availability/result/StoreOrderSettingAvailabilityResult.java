package io.point3.p3api.store.application.setting.availability.result;

import io.point3.p3api.store.application.setting.result.StoreSettingResult;
import java.util.List;
import java.util.UUID;

public record StoreOrderSettingAvailabilityResult(
    UUID storeId,
    String preOrderNotice,
    int cancellationCutoffDays,
    List<StoreOrderSettingDateAvailabilityResult> dates) {

  public static StoreOrderSettingAvailabilityResult from(
      StoreSettingResult setting, List<StoreOrderSettingDateAvailabilityResult> dates) {
    return new StoreOrderSettingAvailabilityResult(
        setting.storeId(),
        setting.preOrderNotice(),
        setting.cancellationCutoffDays(),
        dates);
  }
}
