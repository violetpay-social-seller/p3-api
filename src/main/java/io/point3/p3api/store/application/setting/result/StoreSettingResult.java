package io.point3.p3api.store.application.setting.result;

import io.point3.p3api.store.domain.entity.StoreHoliday;
import io.point3.p3api.store.domain.entity.StoreOperationSetting;
import io.point3.p3api.store.domain.entity.StoreWeeklyPickupSetting;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record StoreSettingResult(
    UUID storeId,
    int leadTimeMinutes,
    String preOrderNotice,
    int cancellationCutoffDays,
    List<WeeklyPickupSetting> weeklyPickupSettings,
    List<LocalDate> holidays) {

  public StoreSettingResult {
    weeklyPickupSettings = List.copyOf(weeklyPickupSettings);
    holidays = List.copyOf(holidays);
  }

  public static StoreSettingResult empty(UUID storeId) {
    return new StoreSettingResult(storeId, 0, null, 0, List.of(), List.of());
  }

  public static StoreSettingResult from(
      StoreOperationSetting setting,
      List<StoreWeeklyPickupSetting> weeklyPickupSettings,
      List<StoreHoliday> holidays) {
    return new StoreSettingResult(
        setting.getStoreId(),
        setting.getLeadTimeMinutes(),
        setting.getPreOrderNotice(),
        setting.getCancellationCutoffDays(),
        weeklyPickupSettings.stream()
            .sorted(Comparator.comparing(StoreWeeklyPickupSetting::getDayOfWeek))
            .map(WeeklyPickupSetting::from)
            .toList(),
        holidays.stream().map(StoreHoliday::getHolidayDate).sorted().toList());
  }

  @Override
  public List<WeeklyPickupSetting> weeklyPickupSettings() {
    return List.copyOf(weeklyPickupSettings);
  }

  @Override
  public List<LocalDate> holidays() {
    return List.copyOf(holidays);
  }

  public record WeeklyPickupSetting(
      DayOfWeek dayOfWeek,
      LocalTime startTime,
      LocalTime endTime,
      int dailyOrderCapacity,
      boolean enabled) {

    private static WeeklyPickupSetting from(StoreWeeklyPickupSetting setting) {
      return new WeeklyPickupSetting(
          setting.getDayOfWeek(),
          setting.getStartTime(),
          setting.getEndTime(),
          setting.getDailyOrderCapacity(),
          setting.isEnabled());
    }
  }
}
