package io.point3.p3api.store.application.setting;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.application.setting.command.UpdateStoreSettingCommand;
import io.point3.p3api.store.application.setting.port.StoreHolidayPersistencePort;
import io.point3.p3api.store.application.setting.port.StoreOperationSettingPersistencePort;
import io.point3.p3api.store.application.setting.port.StoreWeeklyPickupSettingPersistencePort;
import io.point3.p3api.store.application.setting.query.StoreSettingQueryUseCase;
import io.point3.p3api.store.application.setting.result.StoreSettingResult;
import io.point3.p3api.store.application.setting.update.StoreSettingUpdateUseCase;
import io.point3.p3api.store.domain.entity.StoreHoliday;
import io.point3.p3api.store.domain.entity.StoreOperationSetting;
import io.point3.p3api.store.domain.entity.StoreWeeklyPickupSetting;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StoreSettingService implements StoreSettingUpdateUseCase, StoreSettingQueryUseCase {

  private final StorePersistencePort storePersistencePort;
  private final StoreOperationSettingPersistencePort storeOperationSettingPersistencePort;
  private final StoreWeeklyPickupSettingPersistencePort weeklyPickupSettingPersistencePort;
  private final StoreHolidayPersistencePort storeHolidayPersistencePort;
  private final StoreSettingValidator storeSettingValidator;

  @Override
  public StoreSettingResult update(UpdateStoreSettingCommand command) {
    storeSettingValidator.validate(command);
    requireStore(command.storeId());

    StoreOperationSetting setting = saveOperationSetting(command);
    List<StoreWeeklyPickupSetting> weeklyPickupSettings = replaceWeeklyPickupSettings(command);
    List<StoreHoliday> holidays = replaceHolidays(command);

    return StoreSettingResult.from(setting, weeklyPickupSettings, holidays);
  }

  @Override
  @Transactional(readOnly = true)
  public StoreSettingResult getSetting(UUID storeId) {
    requireStore(storeId);
    StoreOperationSetting setting = storeOperationSettingPersistencePort
        .findByStoreId(storeId)
        .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));

    return StoreSettingResult.from(
        setting,
        weeklyPickupSettingPersistencePort.findAllByStoreId(storeId),
        storeHolidayPersistencePort.findAllByStoreId(storeId));
  }

  private StoreOperationSetting saveOperationSetting(UpdateStoreSettingCommand command) {
    StoreOperationSetting setting = storeOperationSettingPersistencePort
        .findByStoreId(command.storeId())
        .orElseGet(() -> StoreOperationSetting.create(
            command.storeId(),
            command.leadTimeMinutes(),
            command.preOrderNotice(),
            command.cancellationCutoffDays()));
    setting.update(
        command.leadTimeMinutes(), command.preOrderNotice(), command.cancellationCutoffDays());
    return storeOperationSettingPersistencePort.save(setting);
  }

  private List<StoreWeeklyPickupSetting> replaceWeeklyPickupSettings(
      UpdateStoreSettingCommand command) {
    weeklyPickupSettingPersistencePort.deleteAllByStoreId(command.storeId());
    return weeklyPickupSettingPersistencePort.saveAll(command.weeklyPickupSettings().stream()
        .map(setting -> StoreWeeklyPickupSetting.create(
            command.storeId(),
            setting.dayOfWeek(),
            setting.startTime(),
            setting.endTime(),
            setting.dailyOrderCapacity(),
            setting.enabled()))
        .toList());
  }

  private List<StoreHoliday> replaceHolidays(UpdateStoreSettingCommand command) {
    storeHolidayPersistencePort.deleteAllByStoreId(command.storeId());
    return storeHolidayPersistencePort.saveAll(command.holidays().stream()
        .map(holiday -> StoreHoliday.create(command.storeId(), holiday))
        .toList());
  }

  private void requireStore(UUID storeId) {
    storePersistencePort
        .findById(storeId)
        .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
  }
}
