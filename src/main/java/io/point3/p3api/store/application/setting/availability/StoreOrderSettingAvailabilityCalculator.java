package io.point3.p3api.store.application.setting.availability;

import io.point3.p3api.store.application.setting.availability.result.StoreOrderSettingDateAvailabilityResult;
import io.point3.p3api.store.application.setting.result.StoreSettingResult;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StoreOrderSettingAvailabilityCalculator {

  private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

  public StoreOrderSettingDateAvailabilityResult calculate(
      LocalDate date,
      StoreSettingResult.WeeklyPickupSetting weeklySetting,
      boolean holiday,
      LocalDate earliestPickupDate,
      LocalDateTime now,
      int cancellationCutoffDays) {
    if (weeklySetting == null || !weeklySetting.enabled() || holiday) {
      return unavailable(date, holiday, cancellationCutoffDays);
    }

    List<LocalTime> pickupSlots = getPickupSlots(date, weeklySetting, earliestPickupDate, now);

    return StoreOrderSettingDateAvailabilityResult.from(
        date,
        !pickupSlots.isEmpty(),
        false,
        pickupSlots,
        cancellationCutoffAt(date, cancellationCutoffDays));
  }

  private StoreOrderSettingDateAvailabilityResult unavailable(
      LocalDate date, boolean holiday, int cancellationCutoffDays) {
    return StoreOrderSettingDateAvailabilityResult.from(
        date, false, holiday, List.of(), cancellationCutoffAt(date, cancellationCutoffDays));
  }

  private List<LocalTime> getPickupSlots(
      LocalDate date,
      StoreSettingResult.WeeklyPickupSetting weeklySetting,
      LocalDate earliestPickupDate,
      LocalDateTime now) {
    if (date.isBefore(earliestPickupDate)) {
      return List.of();
    }
    return java.util.stream.Stream.iterate(
            weeklySetting.startTime(),
            time -> time.isBefore(weeklySetting.endTime()),
            time -> time.plusMinutes(30))
        .filter(time ->
            !date.equals(now.toLocalDate()) || !LocalDateTime.of(date, time).isBefore(now))
        .filter(time -> isOutsideBreakTime(time, weeklySetting))
        .toList();
  }

  private boolean isOutsideBreakTime(
      LocalTime time, StoreSettingResult.WeeklyPickupSetting weeklySetting) {
    LocalTime breakStartTime = weeklySetting.breakStartTime();
    LocalTime breakEndTime = weeklySetting.breakEndTime();
    if (breakStartTime == null || breakEndTime == null) {
      return true;
    }
    return time.isBefore(breakStartTime) || !time.isBefore(breakEndTime);
  }

  private Instant cancellationCutoffAt(LocalDate pickupDate, int cancellationCutoffDays) {
    return pickupDate
        .minusDays(cancellationCutoffDays)
        .atStartOfDay(KOREA_ZONE_ID)
        .toInstant();
  }
}
