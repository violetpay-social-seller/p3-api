package io.point3.p3api.dashboard.application.query;

import io.point3.p3api.chat.application.port.ChatTimelineItemPort;
import io.point3.p3api.dashboard.application.result.SellerDashboardResult;
import io.point3.p3api.dashboard.application.result.SellerRevenueResult;
import io.point3.p3api.inquiry.application.port.InquiryPersistencePort;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.order.application.port.OrderPersistencePort;
import io.point3.p3api.order.application.result.OrderCalendarOrderResult;
import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.type.OrderStatus;
import io.point3.p3api.payment.application.port.RefundPersistencePort;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SellerDashboardService implements SellerDashboardQueryUseCase {

  private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
  private static final int FEE_RATE_BASIS_POINTS = 30;

  private final OrderPersistencePort orderPersistencePort;
  private final RefundPersistencePort refundPersistencePort;
  private final InquiryPersistencePort inquiryPersistencePort;
  private final ChatTimelineItemPort chatTimelineItemPort;
  private final Clock clock;

  @Override
  public SellerDashboardResult getSummary(SellerDashboardQueryCommand command) {
    LocalDate today = LocalDate.now(clock.withZone(KOREA_ZONE));
    LocalDate tomorrow = today.plusDays(1);
    LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate weekEndExclusive = weekStartDate.plusWeeks(1);
    YearMonth currentMonth = YearMonth.from(today);

    List<Order> todayOrders = orderPersistencePort.findCalendarOrders(
        command.storeId(), toInstant(today), toInstant(tomorrow));
    List<Order> weekOrders = orderPersistencePort.findCalendarOrders(
        command.storeId(), toInstant(weekStartDate), toInstant(weekEndExclusive));

    return new SellerDashboardResult(
        today,
        weekStartDate,
        weekEndExclusive.minusDays(1),
        getRevenue(command.storeId(), currentMonth.atDay(1), currentMonth.atEndOfMonth()),
        todayOrders.size(),
        weekOrders.size(),
        orderPersistencePort.countByStoreIdAndStatus(command.storeId(), OrderStatus.PAID),
        orderPersistencePort.countByStoreIdAndStatuses(
            command.storeId(),
            List.of(OrderStatus.CANCEL_REQUESTED, OrderStatus.REFUND_PROCESSING)),
        countUnanswered(command),
        todayOrders.stream()
            .map(order -> OrderCalendarOrderResult.from(order, KOREA_ZONE))
            .toList());
  }

  @Override
  public SellerRevenueResult getRevenue(SellerRevenueQueryCommand command) {
    return getRevenue(command.storeId(), command.startDate(), command.endDate());
  }

  private SellerRevenueResult getRevenue(UUID storeId, LocalDate startDate, LocalDate endDate) {
    Instant startInclusive = toInstant(startDate);
    Instant endExclusive = toInstant(endDate.plusDays(1));
    long paymentRevenueAmount =
        orderPersistencePort.sumSucceededPaymentAmount(storeId, startInclusive, endExclusive);
    long completedRefundAmount =
        refundPersistencePort.sumCompletedAmount(storeId, startInclusive, endExclusive);

    return SellerRevenueResult.of(
        startDate, endDate, paymentRevenueAmount, completedRefundAmount, FEE_RATE_BASIS_POINTS);
  }

  private long countUnanswered(SellerDashboardQueryCommand command) {
    return inquiryPersistencePort.findAllByStoreId(command.storeId()).stream()
        .filter(inquiry -> hasUnreadBuyerMessage(inquiry, command.sellerUserId()))
        .count();
  }

  private boolean hasUnreadBuyerMessage(Inquiry inquiry, UUID sellerUserId) {
    return chatTimelineItemPort.countUnread(
            inquiry.getId(), sellerUserId, inquiry.getSellerLastReadAt())
        > 0;
  }

  private Instant toInstant(LocalDate date) {
    return date.atStartOfDay(KOREA_ZONE).toInstant();
  }
}
