package io.point3.p3api.store.controller.response;

import io.point3.p3api.store.application.setting.result.StoreSettingResult;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record StoreSettingResponse(
    UUID storeId,
    int leadTimeMinutes,
    String preOrderNotice,
    int cancellationCutoffDays,
    List<WeeklyPickupSettingResponse> weeklyPickupSettings,
    List<LocalDate> holidays) {

  public static StoreSettingResponse from(StoreSettingResult result) {
    return new StoreSettingResponse(
        result.storeId(),
        result.leadTimeMinutes(),
        result.preOrderNotice(),
        result.cancellationCutoffDays(),
        result.weeklyPickupSettings().stream().map(WeeklyPickupSettingResponse::from).toList(),
        result.holidays());
  }

  public record WeeklyPickupSettingResponse(
      DayOfWeek dayOfWeek,
      LocalTime startTime,
      LocalTime endTime,
      int dailyOrderCapacity,
      boolean enabled) {

    private static WeeklyPickupSettingResponse from(
        StoreSettingResult.WeeklyPickupSetting setting) {
      return new WeeklyPickupSettingResponse(
          setting.dayOfWeek(),
          setting.startTime(),
          setting.endTime(),
          setting.dailyOrderCapacity(),
          setting.enabled());
    }
  }
}
