package io.point3.p3api.store.controller.request;

import io.point3.p3api.store.application.businesshours.command.UpdateStoreBusinessHoursCommand;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record StoreBusinessHoursRequest(
    @NotEmpty List<@NotNull DayOfWeek> openDays,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    LocalTime breakStartTime,
    LocalTime breakEndTime) {

  public StoreBusinessHoursRequest {
    openDays = openDays == null ? null : List.copyOf(openDays);
  }

  public UpdateStoreBusinessHoursCommand toCommand(UUID storeId) {
    return new UpdateStoreBusinessHoursCommand(
        storeId, openDays, startTime, endTime, breakStartTime, breakEndTime);
  }
}
