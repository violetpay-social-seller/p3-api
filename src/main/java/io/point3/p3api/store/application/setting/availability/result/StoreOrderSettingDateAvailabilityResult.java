package io.point3.p3api.store.application.setting.availability.result;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record StoreOrderSettingDateAvailabilityResult(
    LocalDate date,
    boolean available,
    boolean holiday,
    List<LocalTime> pickupSlots,
    Instant cancellationCutoffAt) {

  public StoreOrderSettingDateAvailabilityResult {
    pickupSlots = List.copyOf(pickupSlots);
  }

  public static StoreOrderSettingDateAvailabilityResult from(
      LocalDate date,
      boolean available,
      boolean holiday,
      List<LocalTime> pickupSlots,
      Instant cancellationCutoffAt) {
    return new StoreOrderSettingDateAvailabilityResult(
        date, available, holiday, pickupSlots, cancellationCutoffAt);
  }

  @Override
  public List<LocalTime> pickupSlots() {
    return List.copyOf(pickupSlots);
  }
}
