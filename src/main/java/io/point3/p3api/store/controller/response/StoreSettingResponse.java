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
    int cancellationCutoffDays,
    List<WeeklyPickupSettingResponse> weeklyPickupSettings,
    List<LocalDate> holidays) {

  public StoreSettingResponse {
    weeklyPickupSettings = List.copyOf(weeklyPickupSettings);
    holidays = List.copyOf(holidays);
  }

  public static StoreSettingResponse from(StoreSettingResult result) {
    return new StoreSettingResponse(
        result.storeId(),
        result.leadTimeMinutes(),
        result.cancellationCutoffDays(),
        result.weeklyPickupSettings().stream()
            .map(WeeklyPickupSettingResponse::from)
            .toList(),
        result.holidays());
  }

  @Override
  public List<WeeklyPickupSettingResponse> weeklyPickupSettings() {
    return List.copyOf(weeklyPickupSettings);
  }

  @Override
  public List<LocalDate> holidays() {
    return List.copyOf(holidays);
  }

  public record WeeklyPickupSettingResponse(
      DayOfWeek dayOfWeek,
      LocalTime startTime,
      LocalTime endTime,
      boolean enabled,
      LocalTime breakStartTime,
      LocalTime breakEndTime) {

    private static WeeklyPickupSettingResponse from(
        StoreSettingResult.WeeklyPickupSetting setting) {
      return new WeeklyPickupSettingResponse(
          setting.dayOfWeek(),
          setting.startTime(),
          setting.endTime(),
          setting.enabled(),
          setting.breakStartTime(),
          setting.breakEndTime());
    }
  }
}
