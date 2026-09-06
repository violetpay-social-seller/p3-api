package io.point3.p3api.store.application.businesshours;

import io.point3.p3api.store.domain.entity.StoreWeeklyPickupSetting;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StoreBusinessHoursTextFormatter {

  public String format(List<StoreWeeklyPickupSetting> settings) {
    if (settings.isEmpty()) {
      return null;
    }

    List<StoreWeeklyPickupSetting> sortedSettings = settings.stream()
        .sorted(Comparator.comparing(StoreWeeklyPickupSetting::getDayOfWeek))
        .toList();
    List<String> descriptions = new ArrayList<>();
    List<StoreWeeklyPickupSetting> openSettings =
        sortedSettings.stream().filter(StoreWeeklyPickupSetting::isEnabled).toList();
    List<StoreWeeklyPickupSetting> closedSettings =
        sortedSettings.stream().filter(setting -> !setting.isEnabled()).toList();

    descriptions.addAll(describeGroups(openSettings, false));
    descriptions.addAll(describeGroups(closedSettings, false));
    if (hasCommonBreakTime(openSettings)) {
      StoreWeeklyPickupSetting first = openSettings.getFirst();
      descriptions.add(
          "휴게시간 " + time(first.getBreakStartTime()) + "~" + time(first.getBreakEndTime()));
    } else {
      descriptions = new ArrayList<>();
      descriptions.addAll(describeGroups(openSettings, true));
      descriptions.addAll(describeGroups(closedSettings, false));
    }
    return String.join(" · ", descriptions);
  }

  private List<String> describeGroups(
      List<StoreWeeklyPickupSetting> settings, boolean includeBreakTime) {
    List<String> descriptions = new ArrayList<>();
    List<StoreWeeklyPickupSetting> group = new ArrayList<>();

    for (StoreWeeklyPickupSetting setting : settings) {
      if (!group.isEmpty()
          && (!isNextDay(group.getLast(), setting)
              || !hasSameSchedule(group.getFirst(), setting))) {
        descriptions.add(describe(group, includeBreakTime));
        group.clear();
      }
      group.add(setting);
    }
    if (!group.isEmpty()) {
      descriptions.add(describe(group, includeBreakTime));
    }
    return descriptions;
  }

  private boolean hasSameSchedule(
      StoreWeeklyPickupSetting first, StoreWeeklyPickupSetting candidate) {
    return first.isEnabled() == candidate.isEnabled()
        && (!first.isEnabled()
            || (first.getStartTime().equals(candidate.getStartTime())
                && first.getEndTime().equals(candidate.getEndTime())
                && java.util.Objects.equals(
                    first.getBreakStartTime(), candidate.getBreakStartTime())
                && java.util.Objects.equals(first.getBreakEndTime(), candidate.getBreakEndTime())));
  }

  private boolean isNextDay(StoreWeeklyPickupSetting previous, StoreWeeklyPickupSetting candidate) {
    return previous.getDayOfWeek().plus(1) == candidate.getDayOfWeek();
  }

  private boolean hasCommonBreakTime(List<StoreWeeklyPickupSetting> openSettings) {
    if (openSettings.isEmpty() || openSettings.getFirst().getBreakStartTime() == null) {
      return false;
    }
    StoreWeeklyPickupSetting first = openSettings.getFirst();
    return openSettings.stream()
        .allMatch(setting -> first.getBreakStartTime().equals(setting.getBreakStartTime())
            && first.getBreakEndTime().equals(setting.getBreakEndTime()));
  }

  private String describe(List<StoreWeeklyPickupSetting> group, boolean includeBreakTime) {
    StoreWeeklyPickupSetting first = group.getFirst();
    String text = dayRange(group);
    if (!first.isEnabled()) {
      return text + " 휴무";
    }

    text += " " + time(first.getStartTime()) + "~" + time(first.getEndTime());
    if (includeBreakTime && first.getBreakStartTime() != null) {
      text += " · 휴게시간 " + time(first.getBreakStartTime()) + "~" + time(first.getBreakEndTime());
    }
    return text;
  }

  private String dayRange(List<StoreWeeklyPickupSetting> group) {
    String first = day(group.getFirst().getDayOfWeek());
    String last = day(group.getLast().getDayOfWeek());
    return group.size() == 1 ? first : first + "~" + last;
  }

  private String time(LocalTime time) {
    return time.getHour() + ":" + String.format("%02d", time.getMinute());
  }

  private String day(DayOfWeek day) {
    return switch (day) {
      case MONDAY -> "월";
      case TUESDAY -> "화";
      case WEDNESDAY -> "수";
      case THURSDAY -> "목";
      case FRIDAY -> "금";
      case SATURDAY -> "토";
      case SUNDAY -> "일";
    };
  }
}
