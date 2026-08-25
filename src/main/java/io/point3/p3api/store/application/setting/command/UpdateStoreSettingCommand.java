package io.point3.p3api.store.application.setting.command;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record UpdateStoreSettingCommand(
    UUID storeId,
    int leadTimeMinutes,
    String preOrderNotice,
    int cancellationCutoffDays,
    List<WeeklyPickupSetting> weeklyPickupSettings,
    List<LocalDate> holidays) {

  public UpdateStoreSettingCommand {
    weeklyPickupSettings = weeklyPickupSettings == null ? List.of() : List.copyOf(weeklyPickupSettings);
    holidays = holidays == null ? List.of() : List.copyOf(holidays);
  }

  public record WeeklyPickupSetting(
      DayOfWeek dayOfWeek,
      LocalTime startTime,
      LocalTime endTime,
      int dailyOrderCapacity,
      boolean enabled) {}
}
