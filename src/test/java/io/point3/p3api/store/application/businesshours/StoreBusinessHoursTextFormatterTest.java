package io.point3.p3api.store.application.businesshours;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.point3.p3api.store.domain.entity.StoreWeeklyPickupSetting;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StoreBusinessHoursTextFormatterTest {

  private final StoreBusinessHoursTextFormatter formatter = new StoreBusinessHoursTextFormatter();

  @Test
  void formatsConsecutiveOpenDaysClosedDaysAndBreakTime() {
    UUID storeId = UUID.randomUUID();

    String result = formatter.format(List.of(
        setting(storeId, DayOfWeek.MONDAY, false),
        setting(storeId, DayOfWeek.TUESDAY, true),
        setting(storeId, DayOfWeek.WEDNESDAY, true),
        setting(storeId, DayOfWeek.THURSDAY, true),
        setting(storeId, DayOfWeek.FRIDAY, true),
        setting(storeId, DayOfWeek.SATURDAY, true),
        setting(storeId, DayOfWeek.SUNDAY, true)));

    assertEquals("화~일 9:00~20:00 · 월 휴무 · 휴게시간 12:00~13:00", result);
  }

  @Test
  void returnsNullWhenWeeklyPickupSettingsAreMissing() {
    assertEquals(null, formatter.format(List.of()));
  }

  private StoreWeeklyPickupSetting setting(UUID storeId, DayOfWeek dayOfWeek, boolean enabled) {
    return StoreWeeklyPickupSetting.create(
        storeId,
        dayOfWeek,
        LocalTime.of(9, 0),
        LocalTime.of(20, 0),
        10,
        LocalTime.of(12, 0),
        LocalTime.of(13, 0),
        enabled);
  }
}
