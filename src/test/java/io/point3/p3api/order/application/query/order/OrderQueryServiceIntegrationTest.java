package io.point3.p3api.order.application.query.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.infrastructure.persistence.InquiryJpaRepository;
import io.point3.p3api.order.application.result.OrderResult;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OrderQueryServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private OrderQueryService orderQueryService;

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
  @DisplayName("판매자 주문 목록은 상태와 결제완료일 기준으로 필터링한다")
  void filtersSellerOrdersByStatusesAndPaidAt() {
    Fixture fixture = prepareFixture("seller-order-filter-paid-at");
    Order paidOrder = saveOrder(
        fixture, "paid", OrderStatus.PAID, "2026-09-01T14:30:00Z", "2026-09-03T04:00:00Z");
    Order pickedUpOrder = saveOrder(
        fixture, "picked", OrderStatus.PICKED_UP, "2026-09-01T15:30:00Z", "2026-09-04T04:00:00Z");
    saveOrder(
        fixture, "refunded", OrderStatus.REFUNDED, "2026-09-02T02:00:00Z", "2026-09-05T04:00:00Z");

    List<OrderResult> results = orderQueryService.getSellerOrders(SellerOrderListQuery.of(
        fixture.store().getId(),
        List.of("PAID|PICKED_UP"),
        LocalDate.parse("2026-09-02"),
        LocalDate.parse("2026-09-02"),
        "PAID_AT"));

    assertEquals(1, results.size());
    assertEquals(pickedUpOrder.getId(), results.getFirst().id());
    assertEquals(OrderStatus.PICKED_UP, results.getFirst().status());
    assertEquals(OrderStatus.PAID, paidOrder.getStatus());
  }

  @Test
  @DisplayName("판매자 주문 목록은 픽업일 기준으로 필터링한다")
  void filtersSellerOrdersByPickupAt() {
    Fixture fixture = prepareFixture("seller-order-filter-pickup-at");
    Order firstOrder = saveOrder(
        fixture, "first", OrderStatus.PAID, "2026-09-01T01:00:00Z", "2026-09-02T04:00:00Z");
    saveOrder(fixture, "second", OrderStatus.PAID, "2026-09-01T02:00:00Z", "2026-09-05T04:00:00Z");

    List<OrderResult> results = orderQueryService.getSellerOrders(SellerOrderListQuery.of(
        fixture.store().getId(),
        List.of("PAID"),
        LocalDate.parse("2026-09-02"),
        LocalDate.parse("2026-09-02"),
        "PICKUP_AT"));

    assertEquals(1, results.size());
    assertEquals(firstOrder.getId(), results.getFirst().id());
  }

  @Test
  @DisplayName("판매자 주문 목록 필터는 잘못된 입력을 거절한다")
  void rejectsInvalidSellerOrderFilter() {
    UUID storeId = UUID.randomUUID();

    BaseException statusException = assertThrows(
        BaseException.class,
        () -> SellerOrderListQuery.of(storeId, List.of("UNKNOWN"), null, null, null));
    BaseException dateBasisException = assertThrows(
        BaseException.class, () -> SellerOrderListQuery.of(storeId, null, null, null, "UNKNOWN"));
    BaseException dateRangeException = assertThrows(
        BaseException.class,
        () -> orderQueryService.getSellerOrders(SellerOrderListQuery.of(
            storeId, null, LocalDate.parse("2026-09-03"), LocalDate.parse("2026-09-02"), null)));

    assertEquals(CommonErrorCode.INVALID_INPUT, statusException.getErrorCode());
    assertEquals(CommonErrorCode.INVALID_INPUT, dateBasisException.getErrorCode());
    assertEquals(CommonErrorCode.INVALID_INPUT, dateRangeException.getErrorCode());
  }

  private Fixture prepareFixture(String prefix) {
    User seller = saveUser(UserRole.SELLER, prefix + "-seller");
    User buyer = saveUser(UserRole.BUYER, prefix + "-buyer");
    Store store = storeJpaRepository.saveAndFlush(
        Store.create(seller.getId(), "주문 테스트 스토어 " + prefix, "order-test-" + UUID.randomUUID()));
    Inquiry inquiry =
        inquiryJpaRepository.saveAndFlush(Inquiry.create(store.getId(), buyer.getId()));
    return new Fixture(seller, buyer, store, inquiry);
  }

  private Order saveOrder(
      Fixture fixture, String suffix, OrderStatus status, String paidAt, String pickupAt) {
    OrderConfirmation confirmation = orderConfirmationJpaRepository.saveAndFlush(
        createConfirmation(fixture.inquiry().getId(), fixture.seller().getId(), pickupAt));
    PaymentAttempt paymentAttempt = paymentAttemptJpaRepository.saveAndFlush(
        createPaymentAttempt(confirmation, fixture.buyer(), paidAt));
    Order order = Order.create(
        fixture.store().getId(),
        fixture.buyer().getId(),
        fixture.inquiry().getId(),
        confirmation.getId(),
        paymentAttempt.getId(),
        "P3-20260901-" + suffix + "-" + UUID.randomUUID().toString().substring(0, 8),
        confirmation.getMenuName(),
        confirmation.getOptionSummary(),
        paymentAttempt.getAmount(),
        confirmation.getPickupAt());
    if (status == OrderStatus.PICKED_UP) {
      order.markPickedUp();
    }
    if (status == OrderStatus.REFUNDED) {
      order.requestRefund("테스트 환불 요청", Instant.parse(paidAt));
      order.refund("테스트 환불");
    }
    return orderJpaRepository.saveAndFlush(order);
  }

  private OrderConfirmation createConfirmation(UUID inquiryId, UUID sellerUserId, String pickupAt) {
    OrderConfirmation confirmation = OrderConfirmation.create(
        inquiryId,
        null,
        sellerUserId,
        "초코 케이크 1호",
        "딸기 토핑",
        41000,
        Instant.parse(pickupAt),
        "주문 테스트 스토어",
        null,
        null,
        "픽업 전 연락");
    confirmation.sent(Instant.parse("2026-08-30T01:00:00Z"));
    confirmation.markPaid();
    return confirmation;
  }

  private PaymentAttempt createPaymentAttempt(
      OrderConfirmation confirmation, User buyer, String paidAt) {
    PaymentAttempt paymentAttempt = PaymentAttempt.create(
        confirmation.getId(),
        buyer.getId(),
        "pymt_sess-" + UUID.randomUUID(),
        null,
        confirmation.getAmount(),
        Instant.parse("2026-09-10T01:00:00Z"));
    paymentAttempt.succeed("payer-" + UUID.randomUUID(), Instant.parse(paidAt));
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

  private record Fixture(User seller, User buyer, Store store, Inquiry inquiry) {}
}
