package io.point3.p3api.store.application.businesshours;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.application.businesshours.command.UpdateStoreBusinessHoursCommand;
import io.point3.p3api.store.application.businesshours.query.StoreBusinessHoursQueryUseCase;
import io.point3.p3api.store.application.businesshours.result.StoreBusinessHoursResult;
import io.point3.p3api.store.application.businesshours.update.StoreBusinessHoursUpdateUseCase;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.application.setting.port.StoreWeeklyPickupSettingPersistencePort;
import io.point3.p3api.store.domain.entity.StoreWeeklyPickupSetting;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StoreBusinessHoursService
    implements StoreBusinessHoursQueryUseCase, StoreBusinessHoursUpdateUseCase {

  private final StorePersistencePort storePersistencePort;
  private final StoreWeeklyPickupSettingPersistencePort weeklyPickupSettingPersistencePort;

  @Override
  @Transactional(readOnly = true)
  public StoreBusinessHoursResult getBusinessHours(UUID storeId) {
    requireStore(storeId);
    List<StoreWeeklyPickupSetting> openSettings = weeklyPickupSettingPersistencePort
        .findAllByStoreId(storeId).stream()
        .filter(StoreWeeklyPickupSetting::isEnabled)
        .toList();
    if (openSettings.isEmpty()) {
      return new StoreBusinessHoursResult(List.of(), null, null, null, null);
    }
    StoreWeeklyPickupSetting reference = openSettings.getFirst();
    boolean hasDifferentHours = openSettings.stream().anyMatch(setting ->
        !setting.getStartTime().equals(reference.getStartTime())
            || !setting.getEndTime().equals(reference.getEndTime())
            || !java.util.Objects.equals(setting.getBreakStartTime(), reference.getBreakStartTime())
            || !java.util.Objects.equals(setting.getBreakEndTime(), reference.getBreakEndTime()));
    if (hasDifferentHours) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }
    return new StoreBusinessHoursResult(
        openSettings.stream().map(StoreWeeklyPickupSetting::getDayOfWeek).sorted().toList(),
        reference.getStartTime(),
        reference.getEndTime(),
        reference.getBreakStartTime(),
        reference.getBreakEndTime());
  }

  @Override
  public StoreBusinessHoursResult updateBusinessHours(UpdateStoreBusinessHoursCommand command) {
    validate(command);
    requireStore(command.storeId());
    Map<DayOfWeek, StoreWeeklyPickupSetting> existing = weeklyPickupSettingPersistencePort
        .findAllByStoreId(command.storeId()).stream()
        .collect(java.util.stream.Collectors.toMap(
            StoreWeeklyPickupSetting::getDayOfWeek, Function.identity()));
    EnumSet<DayOfWeek> openDays = EnumSet.copyOf(command.openDays());
    List<StoreWeeklyPickupSetting> replacements = java.util.Arrays.stream(DayOfWeek.values())
        .filter(day -> openDays.contains(day) || existing.containsKey(day))
        .map(day -> replacement(command, day, existing.get(day), openDays.contains(day)))
        .sorted(Comparator.comparing(StoreWeeklyPickupSetting::getDayOfWeek))
        .toList();
    weeklyPickupSettingPersistencePort.deleteAllByStoreId(command.storeId());
    weeklyPickupSettingPersistencePort.saveAll(replacements);
    return new StoreBusinessHoursResult(
        openDays.stream().sorted().toList(),
        command.startTime(),
        command.endTime(),
        command.breakStartTime(),
        command.breakEndTime());
  }

  private StoreWeeklyPickupSetting replacement(
      UpdateStoreBusinessHoursCommand command,
      DayOfWeek day,
      StoreWeeklyPickupSetting existing,
      boolean open) {
    if (open) {
      return StoreWeeklyPickupSetting.create(
          command.storeId(),
          day,
          command.startTime(),
          command.endTime(),
          existing == null ? null : existing.getDailyOrderCapacity(),
          command.breakStartTime(),
          command.breakEndTime(),
          true);
    }
    return StoreWeeklyPickupSetting.create(
        command.storeId(),
        existing.getDayOfWeek(),
        existing.getStartTime(),
        existing.getEndTime(),
        existing.getDailyOrderCapacity(),
        existing.getBreakStartTime(),
        existing.getBreakEndTime(),
        false);
  }

  private void validate(UpdateStoreBusinessHoursCommand command) {
    if (command.storeId() == null || command.openDays().isEmpty()
        || command.openDays().stream().distinct().count() != command.openDays().size()
        || command.startTime() == null || command.endTime() == null
        || !isHalfHourly(command.startTime()) || !isHalfHourly(command.endTime())
        || !command.startTime().isBefore(command.endTime())
        || !hasValidBreakTime(command)) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }
  }

  private boolean hasValidBreakTime(UpdateStoreBusinessHoursCommand command) {
    if (command.breakStartTime() == null && command.breakEndTime() == null) return true;
    if (command.breakStartTime() == null || command.breakEndTime() == null) return false;
    return isHalfHourly(command.breakStartTime()) && isHalfHourly(command.breakEndTime())
        && command.breakStartTime().isBefore(command.breakEndTime())
        && !command.breakStartTime().isBefore(command.startTime())
        && !command.breakEndTime().isAfter(command.endTime());
  }

  private boolean isHalfHourly(LocalTime time) {
    return time.getMinute() % 30 == 0 && time.getSecond() == 0 && time.getNano() == 0;
  }

  private void requireStore(UUID storeId) {
    if (storePersistencePort.findById(storeId).isEmpty()) {
      throw new BaseException(StoreErrorCode.STORE_NOT_FOUND);
    }
  }
}
