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
      LocalDateTime earliestPickupAt,
      int cancellationCutoffDays) {
    if (weeklySetting == null || !weeklySetting.enabled() || holiday) {
      return unavailable(date, holiday, cancellationCutoffDays);
    }

    List<LocalTime> pickupSlots = getPickupSlots(date, weeklySetting, earliestPickupAt);

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
      LocalDateTime earliestPickupAt) {
    return java.util.stream.Stream.iterate(
            weeklySetting.startTime(),
            time -> time.isBefore(weeklySetting.endTime()),
            time -> time.plusMinutes(30))
        .filter(time -> !LocalDateTime.of(date, time).isBefore(earliestPickupAt))
        .toList();
  }

  private Instant cancellationCutoffAt(LocalDate pickupDate, int cancellationCutoffDays) {
    return pickupDate
        .minusDays(cancellationCutoffDays)
        .atStartOfDay(KOREA_ZONE_ID)
        .toInstant();
  }
}
