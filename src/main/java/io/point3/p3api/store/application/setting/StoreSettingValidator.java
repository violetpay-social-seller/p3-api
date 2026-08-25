package io.point3.p3api.store.application.setting;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.store.application.setting.command.UpdateStoreSettingCommand;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class StoreSettingValidator {

  public void validate(UpdateStoreSettingCommand command) {
    if (command.storeId() == null
        || command.leadTimeMinutes() < 0
        || command.leadTimeMinutes() % 30 != 0
        || command.cancellationCutoffDays() < 0) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }

    validateWeeklyPickupSettings(command.weeklyPickupSettings());
    validateHolidays(command.holidays());
  }

  private void validateWeeklyPickupSettings(
      List<UpdateStoreSettingCommand.WeeklyPickupSetting> settings) {
    Set<DayOfWeek> days = new HashSet<>();
    for (UpdateStoreSettingCommand.WeeklyPickupSetting setting : settings) {
      if (setting == null
          || setting.dayOfWeek() == null
          || setting.startTime() == null
          || setting.endTime() == null
          || !days.add(setting.dayOfWeek())
          || !isHalfHourly(setting.startTime())
          || !isHalfHourly(setting.endTime())
          || !setting.startTime().isBefore(setting.endTime())
          || setting.dailyOrderCapacity() <= 0) {
        throw new BaseException(CommonErrorCode.INVALID_INPUT);
      }
    }
  }

  private void validateHolidays(List<LocalDate> holidays) {
    Set<LocalDate> holidayDates = new HashSet<>();
    for (LocalDate holiday : holidays) {
      if (holiday == null || !holidayDates.add(holiday)) {
        throw new BaseException(CommonErrorCode.INVALID_INPUT);
      }
    }
  }

  private boolean isHalfHourly(LocalTime time) {
    return time.getMinute() % 30 == 0 && time.getSecond() == 0 && time.getNano() == 0;
  }
}
