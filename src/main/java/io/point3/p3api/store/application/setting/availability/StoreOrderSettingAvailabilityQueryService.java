package io.point3.p3api.store.application.setting.availability;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.order.application.port.OrderPersistencePort;
import io.point3.p3api.order.application.result.OrderPickupDateCount;
import io.point3.p3api.order.domain.type.OrderStatus;
import io.point3.p3api.store.application.setting.availability.result.StoreOrderSettingAvailabilityResult;
import io.point3.p3api.store.application.setting.query.StoreSettingQueryUseCase;
import io.point3.p3api.store.application.setting.result.StoreSettingResult;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreOrderSettingAvailabilityQueryService
    implements StoreOrderSettingAvailabilityQueryUseCase {

  private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
  private static final long MAX_QUERY_DAYS = 31;
  private static final Set<OrderStatus> OCCUPYING_ORDER_STATUSES = Set.of(
      OrderStatus.PAID,
      OrderStatus.CANCEL_REQUESTED,
      OrderStatus.REFUND_PROCESSING,
      OrderStatus.PICKED_UP);

  private final StoreSettingQueryUseCase storeSettingQueryUseCase;
  private final OrderPersistencePort orderPersistencePort;
  private final StoreOrderSettingAvailabilityCalculator availabilityCalculator;
  private final Clock clock;

  @Override
  public StoreOrderSettingAvailabilityResult getAvailability(
      UUID storeId, LocalDate from, LocalDate to) {
    validateRange(from, to);

    StoreSettingResult setting = storeSettingQueryUseCase.getSetting(storeId);
    Map<java.time.DayOfWeek, StoreSettingResult.WeeklyPickupSetting> weeklySettings =
        toWeeklySettings(setting);
    Set<LocalDate> holidays = Set.copyOf(setting.holidays());
    Map<LocalDate, Long> occupiedCounts = getOccupiedCounts(storeId, from, to);
    LocalDateTime earliestPickupAt = LocalDateTime.ofInstant(clock.instant(), KOREA_ZONE_ID)
        .plusMinutes(setting.leadTimeMinutes());

    return StoreOrderSettingAvailabilityResult.from(
        setting,
        from.datesUntil(to.plusDays(1))
            .map(date -> availabilityCalculator.calculate(
                date,
                weeklySettings.get(date.getDayOfWeek()),
                holidays.contains(date),
                occupiedCounts.getOrDefault(date, 0L),
                earliestPickupAt,
                setting.cancellationCutoffDays()))
            .toList());
  }

  private void validateRange(LocalDate from, LocalDate to) {
    if (from == null
        || to == null
        || from.isAfter(to)
        || ChronoUnit.DAYS.between(from, to) + 1 > MAX_QUERY_DAYS) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }
  }

  private Map<java.time.DayOfWeek, StoreSettingResult.WeeklyPickupSetting> toWeeklySettings(
      StoreSettingResult setting) {
    Map<java.time.DayOfWeek, StoreSettingResult.WeeklyPickupSetting> weeklySettings =
        new EnumMap<>(java.time.DayOfWeek.class);
    setting.weeklyPickupSettings().forEach(item -> weeklySettings.put(item.dayOfWeek(), item));
    return weeklySettings;
  }

  private Map<LocalDate, Long> getOccupiedCounts(UUID storeId, LocalDate from, LocalDate to) {
    Instant fromInclusive = from.atStartOfDay(KOREA_ZONE_ID).toInstant();
    Instant toExclusive = to.plusDays(1).atStartOfDay(KOREA_ZONE_ID).toInstant();
    return orderPersistencePort
        .countByStoreIdAndPickupAtBetween(
            storeId, fromInclusive, toExclusive, OCCUPYING_ORDER_STATUSES)
        .stream()
        .collect(Collectors.toMap(OrderPickupDateCount::pickupDate, OrderPickupDateCount::orderCount));
  }
}
