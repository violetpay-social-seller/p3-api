package io.point3.p3api.order.application.calendar;

import io.point3.p3api.order.application.port.OrderPersistencePort;
import io.point3.p3api.order.application.result.OrderCalendarDayResult;
import io.point3.p3api.order.application.result.OrderCalendarOrderResult;
import io.point3.p3api.order.application.result.OrderCalendarResult;
import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderCalendarService implements OrderCalendarQueryUseCase {

  private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

  private final OrderPersistencePort orderPersistencePort;
  private final Clock clock;

  @Override
  public OrderCalendarResult getToday(UUID storeId, OrderStatus status) {
    LocalDate today = LocalDate.now(clock.withZone(KOREA_ZONE));

    return getRange(storeId, today, today.plusDays(1), status);
  }

  @Override
  public OrderCalendarResult getThisWeek(UUID storeId, OrderStatus status) {
    LocalDate today = LocalDate.now(clock.withZone(KOREA_ZONE));
    LocalDate startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

    return getRange(storeId, startDate, startDate.plusWeeks(1), status);
  }

  @Override
  public OrderCalendarResult getMonth(UUID storeId, int year, int month, OrderStatus status) {
    YearMonth yearMonth = YearMonth.of(year, month);
    LocalDate startDate = yearMonth.atDay(1);

    return getRange(storeId, startDate, yearMonth.plusMonths(1).atDay(1), status);
  }

  private OrderCalendarResult getRange(
      UUID storeId, LocalDate startDate, LocalDate endDateExclusive, OrderStatus status) {
    Instant startInclusive = startDate.atStartOfDay(KOREA_ZONE).toInstant();
    Instant endExclusive = endDateExclusive.atStartOfDay(KOREA_ZONE).toInstant();

    List<Order> orders = status == null
        ? orderPersistencePort.findCalendarOrders(storeId, startInclusive, endExclusive)
        : orderPersistencePort.findCalendarOrdersByStatus(
            storeId, status, startInclusive, endExclusive);
    Map<LocalDate, List<OrderCalendarOrderResult>> ordersByDate = orders.stream()
        .map(order -> OrderCalendarOrderResult.from(order, KOREA_ZONE))
        .collect(Collectors.groupingBy(OrderCalendarOrderResult::pickupDate));
    List<OrderCalendarDayResult> days = startDate
        .datesUntil(endDateExclusive)
        .map(date -> OrderCalendarDayResult.of(date, ordersByDate.getOrDefault(date, List.of())))
        .toList();

    return OrderCalendarResult.of(startDate, endDateExclusive.minusDays(1), status, days);
  }
}
