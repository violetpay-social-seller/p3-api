package io.point3.p3api.store.controller.response;

import io.point3.p3api.store.application.businesshours.result.StoreBusinessHoursResult;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record StoreBusinessHoursResponse(
    List<DayOfWeek> openDays,
    LocalTime startTime,
    LocalTime endTime,
    LocalTime breakStartTime,
    LocalTime breakEndTime) {

  public StoreBusinessHoursResponse {
    openDays = List.copyOf(openDays);
  }

  public static StoreBusinessHoursResponse from(StoreBusinessHoursResult result) {
    return new StoreBusinessHoursResponse(
        result.openDays(),
        result.startTime(),
        result.endTime(),
        result.breakStartTime(),
        result.breakEndTime());
  }

  @Override
  public List<DayOfWeek> openDays() {
    return List.copyOf(openDays);
  }
}
