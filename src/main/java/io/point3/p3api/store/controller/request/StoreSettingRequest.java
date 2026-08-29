package io.point3.p3api.store.controller.request;

import io.point3.p3api.store.application.setting.command.UpdateStoreSettingCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record StoreSettingRequest(
    @Min(0) int leadTimeMinutes,
    String preOrderNotice,
    @Min(0) int cancellationCutoffDays,
    @NotNull List<@Valid WeeklyPickupSettingRequest> weeklyPickupSettings,
    @NotNull List<LocalDate> holidays) {

  public StoreSettingRequest {
    weeklyPickupSettings = weeklyPickupSettings == null ? null : List.copyOf(weeklyPickupSettings);
    holidays = holidays == null ? null : List.copyOf(holidays);
  }

  public UpdateStoreSettingCommand toCommand(UUID storeId) {
    return new UpdateStoreSettingCommand(
        storeId,
        leadTimeMinutes,
        preOrderNotice,
        cancellationCutoffDays,
        weeklyPickupSettings.stream().map(WeeklyPickupSettingRequest::toCommand).toList(),
        holidays);
  }

  @Override
  public List<WeeklyPickupSettingRequest> weeklyPickupSettings() {
    return weeklyPickupSettings == null ? null : List.copyOf(weeklyPickupSettings);
  }

  @Override
  public List<LocalDate> holidays() {
    return holidays == null ? null : List.copyOf(holidays);
  }

  public record WeeklyPickupSettingRequest(
      @NotNull DayOfWeek dayOfWeek,
      @NotNull LocalTime startTime,
      @NotNull LocalTime endTime,
      @Min(1) int dailyOrderCapacity,
      boolean enabled) {

    private UpdateStoreSettingCommand.WeeklyPickupSetting toCommand() {
      return new UpdateStoreSettingCommand.WeeklyPickupSetting(
          dayOfWeek, startTime, endTime, dailyOrderCapacity, enabled);
    }
  }
}
