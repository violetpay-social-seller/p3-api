package io.point3.p3api.store.controller.response;

import io.point3.p3api.store.application.setting.availability.result.StoreOrderSettingAvailabilityResult;
import io.point3.p3api.store.application.setting.availability.result.StoreOrderSettingDateAvailabilityResult;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record StoreOrderSettingAvailabilityResponse(
    UUID storeId,
    String preOrderNotice,
    int cancellationCutoffDays,
    List<DateAvailabilityResponse> dates) {

  public static StoreOrderSettingAvailabilityResponse from(
      StoreOrderSettingAvailabilityResult result) {
    return new StoreOrderSettingAvailabilityResponse(
        result.storeId(),
        result.preOrderNotice(),
        result.cancellationCutoffDays(),
        result.dates().stream().map(DateAvailabilityResponse::from).toList());
  }

  public record DateAvailabilityResponse(
      LocalDate date,
      boolean available,
      boolean holiday,
      List<LocalTime> pickupSlots,
      int dailyOrderCapacity,
      int remainingOrderCapacity,
      Instant cancellationCutoffAt) {

    private static DateAvailabilityResponse from(
        StoreOrderSettingDateAvailabilityResult availability) {
      return new DateAvailabilityResponse(
          availability.date(),
          availability.available(),
          availability.holiday(),
          availability.pickupSlots(),
          availability.dailyOrderCapacity(),
          availability.remainingOrderCapacity(),
          availability.cancellationCutoffAt());
    }
  }
}
