package io.point3.p3api.store.application.businesshours.command;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record UpdateStoreBusinessHoursCommand(
    UUID storeId,
    List<DayOfWeek> openDays,
    LocalTime startTime,
    LocalTime endTime,
    LocalTime breakStartTime,
    LocalTime breakEndTime) {

  public UpdateStoreBusinessHoursCommand {
    openDays = openDays == null ? List.of() : List.copyOf(openDays);
  }
}
