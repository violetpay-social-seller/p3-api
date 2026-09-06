package io.point3.p3api.store.application.businesshours.result;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record StoreBusinessHoursResult(
    List<DayOfWeek> openDays,
    LocalTime startTime,
    LocalTime endTime,
    LocalTime breakStartTime,
    LocalTime breakEndTime) {

  public StoreBusinessHoursResult {
    openDays = List.copyOf(openDays);
  }

  @Override
  public List<DayOfWeek> openDays() {
    return List.copyOf(openDays);
  }
}
