package io.point3.p3api.order.application.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.infrastructure.persistence.InquiryJpaRepository;
import io.point3.p3api.order.application.result.OrderCalendarResult;
import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderStatus;
import io.point3.p3api.order.infrastructure.persistence.OrderConfirmationJpaRepository;
import io.point3.p3api.order.infrastructure.persistence.OrderJpaRepository;
import io.point3.p3api.payment.domain.entity.PaymentAttempt;
import io.point3.p3api.payment.infrastructure.persistence.PaymentAttemptJpaRepository;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.store.infrastructure.persistence.StoreJpaRepository;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

class OrderCalendarServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private OrderCalendarQueryUseCase orderCalendarQueryUseCase;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private StoreJpaRepository storeJpaRepository;

  @Autowired
  private InquiryJpaRepository inquiryJpaRepository;

  @Autowired
  private OrderConfirmationJpaRepository orderConfirmationJpaRepository;

  @Autowired
  private PaymentAttemptJpaRepository paymentAttemptJpaRepository;

  @Autowired
  private OrderJpaRepository orderJpaRepository;

  @Test
  @DisplayName("오늘 주문은 한국 시간 기준 하루 범위에서 픽업 시간순으로 조회한다")
  void getsTodayOrdersByKoreaDate() {
    Fixture fixture = prepareFixture("calendar-today");
    Order midnightOrder = saveOrder(fixture, "today-1", Instant.parse("2026-08-24T15:30:00Z"));
    Order morningOrder = saveOrder(fixture, "today-2", Instant.parse("2026-08-25T00:30:00Z"));
    saveOrder(fixture, "tomorrow", Instant.parse("2026-08-25T15:00:00Z"));

    OrderCalendarResult result =
        orderCalendarQueryUseCase.getToday(fixture.store().getId(), null);

    assertEquals(LocalDate.parse("2026-08-25"), result.startDate());
    assertEquals(LocalDate.parse("2026-08-25"), result.endDate());
    assertEquals(1, result.days().size());
    assertEquals(2, result.totalOrderCount());
    assertEquals(2, result.days().get(0).orderCount());
    assertEquals(midnightOrder.getId(), result.days().get(0).orders().get(0).orderId());
    assertEquals(morningOrder.getId(), result.days().get(0).orders().get(1).orderId());
    assertEquals(fixture.inquiryId(), result.days().get(0).orders().get(0).inquiryId());
  }

  @Test
  @DisplayName("이번 주 주문은 한국 시간 기준 월요일부터 일요일까지 조회한다")
  void getsThisWeekOrders() {
    Fixture fixture = prepareFixture("calendar-week");
    Order mondayOrder = saveOrder(fixture, "week-1", Instant.parse("2026-08-24T00:00:00Z"));
    Order sundayOrder = saveOrder(fixture, "week-2", Instant.parse("2026-08-30T12:00:00Z"));
    saveOrder(fixture, "next-week", Instant.parse("2026-08-30T15:00:00Z"));
    saveOrder(
        prepareFixture("calendar-other"), "other-store", Instant.parse("2026-08-25T01:00:00Z"));

    OrderCalendarResult result =
        orderCalendarQueryUseCase.getThisWeek(fixture.store().getId(), null);

    assertEquals(LocalDate.parse("2026-08-24"), result.startDate());
    assertEquals(LocalDate.parse("2026-08-30"), result.endDate());
    assertEquals(7, result.days().size());
    assertEquals(2, result.totalOrderCount());
    assertEquals(mondayOrder.getId(), result.days().get(0).orders().get(0).orderId());
    assertEquals(sundayOrder.getId(), result.days().get(6).orders().get(0).orderId());
  }

  @Test
  @DisplayName("월별 캘린더는 날짜별 주문 건수와 상태 필터를 반환한다")
  void getsMonthlyCalendarWithStatus() {
    Fixture fixture = prepareFixture("calendar-month");
    Order paidOrder = saveOrder(fixture, "month-paid", Instant.parse("2026-08-10T02:00:00Z"));
    Order pickedUpOrder =
        saveOrder(fixture, "month-picked-up", Instant.parse("2026-08-10T04:00:00Z"));
    pickedUpOrder.markPickedUp();
    orderJpaRepository.saveAndFlush(pickedUpOrder);
    saveOrder(fixture, "other-month", Instant.parse("2026-09-01T00:00:00Z"));

    OrderCalendarResult allResult =
        orderCalendarQueryUseCase.getMonth(fixture.store().getId(), 2026, 8, null);
    OrderCalendarResult paidResult =
        orderCalendarQueryUseCase.getMonth(fixture.store().getId(), 2026, 8, OrderStatus.PAID);

    assertEquals(LocalDate.parse("2026-08-01"), allResult.startDate());
    assertEquals(LocalDate.parse("2026-08-31"), allResult.endDate());
    assertEquals(31, allResult.days().size());
    assertEquals(2, allResult.totalOrderCount());
    assertEquals(2, allResult.days().get(9).orderCount());
    assertEquals(1, paidResult.totalOrderCount());
    assertEquals(OrderStatus.PAID, paidResult.status());
    assertEquals(paidOrder.getId(), paidResult.days().get(9).orders().get(0).orderId());
  }

  private Fixture prepareFixture(String prefix) {
    User seller = saveUser(UserRole.SELLER, prefix + "-seller");
    User buyer = saveUser(UserRole.BUYER, prefix + "-buyer");
    Store store = storeJpaRepository.saveAndFlush(Store.create(
        seller.getId(), "캘린더 테스트 스토어 " + prefix, "calendar-test-" + UUID.randomUUID()));
    Inquiry inquiry =
        inquiryJpaRepository.saveAndFlush(Inquiry.create(store.getId(), buyer.getId()));

    return new Fixture(seller, buyer, store, inquiry.getId());
  }

  private Order saveOrder(Fixture fixture, String suffix, Instant pickupAt) {
    OrderConfirmation confirmation = orderConfirmationJpaRepository.saveAndFlush(
        createConfirmation(fixture.inquiryId(), fixture.seller().getId(), pickupAt));
    PaymentAttempt paymentAttempt = paymentAttemptJpaRepository.saveAndFlush(
        createPaymentAttempt(confirmation, fixture.buyer()));

    return orderJpaRepository.saveAndFlush(Order.create(
        fixture.store().getId(),
        fixture.buyer().getId(),
        fixture.inquiryId(),
        confirmation.getId(),
        paymentAttempt.getId(),
        "P3-20260825-" + suffix,
        confirmation.getMenuName(),
        confirmation.getOptionSummary(),
        paymentAttempt.getAmount(),
        confirmation.getPickupAt()));
  }

  private OrderConfirmation createConfirmation(
      UUID inquiryId, UUID sellerUserId, Instant pickupAt) {
    OrderConfirmation confirmation = OrderConfirmation.create(
        inquiryId,
        null,
        sellerUserId,
        "초코 케이크",
        "딸기 토핑",
        41000,
        pickupAt,
        "캘린더 테스트 스토어",
        null,
        null,
        null);
    confirmation.sent(Instant.parse("2026-08-24T01:00:00Z"));
    confirmation.markPaid();
    return confirmation;
  }

  private PaymentAttempt createPaymentAttempt(OrderConfirmation confirmation, User buyer) {
    PaymentAttempt paymentAttempt = PaymentAttempt.create(
        confirmation.getId(),
        buyer.getId(),
        "pymt_sess-" + UUID.randomUUID(),
        null,
        confirmation.getAmount(),
        Instant.parse("2026-08-25T03:00:00Z"));
    paymentAttempt.succeed("payer-" + UUID.randomUUID(), Instant.parse("2026-08-24T02:00:00Z"));
    return paymentAttempt;
  }

  private User saveUser(UserRole role, String prefix) {
    return userJpaRepository.saveAndFlush(User.create(
        UUID.randomUUID().toString(),
        uniqueEmail(prefix),
        prefix,
        role,
        "010-0000-0000",
        SignupProvider.GOOGLE));
  }

  private record Fixture(User seller, User buyer, Store store, UUID inquiryId) {}

  @TestConfiguration
  static class CalendarClockTestConfiguration {

    @Bean
    @Primary
    Clock fixedCalendarClock() {
      return Clock.fixed(Instant.parse("2026-08-25T02:00:00Z"), ZoneOffset.UTC);
    }
  }
}
