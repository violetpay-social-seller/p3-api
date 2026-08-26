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
      long occupiedCount,
      LocalDateTime earliestPickupAt,
      int cancellationCutoffDays) {
    if (weeklySetting == null || !weeklySetting.enabled() || holiday) {
      return unavailable(date, holiday, cancellationCutoffDays);
    }

    List<LocalTime> pickupSlots = getPickupSlots(date, weeklySetting, earliestPickupAt);
    int remainingOrderCapacity =
        (int) Math.max(weeklySetting.dailyOrderCapacity() - occupiedCount, 0);

    return StoreOrderSettingDateAvailabilityResult.from(
        date,
        !pickupSlots.isEmpty() && remainingOrderCapacity > 0,
        false,
        pickupSlots,
        weeklySetting.dailyOrderCapacity(),
        remainingOrderCapacity,
        cancellationCutoffAt(date, cancellationCutoffDays));
  }

  private StoreOrderSettingDateAvailabilityResult unavailable(
      LocalDate date, boolean holiday, int cancellationCutoffDays) {
    return StoreOrderSettingDateAvailabilityResult.from(
        date, false, holiday, List.of(), 0, 0, cancellationCutoffAt(date, cancellationCutoffDays));
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
