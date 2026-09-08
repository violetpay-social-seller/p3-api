package io.point3.p3api.order.application.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderErrorCode;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.type.InquiryStatus;
import io.point3.p3api.inquiry.infrastructure.persistence.InquiryJpaRepository;
import io.point3.p3api.notification.domain.type.NotificationType;
import io.point3.p3api.notification.infrastructure.persistence.NotificationJpaRepository;
import io.point3.p3api.order.application.query.order.OrderQueryUseCase;
import io.point3.p3api.order.application.result.OrderDetailResult;
import io.point3.p3api.order.application.result.OrderResult;
import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderStatus;
import io.point3.p3api.order.infrastructure.persistence.OrderConfirmationJpaRepository;
import io.point3.p3api.order.infrastructure.persistence.OrderJpaRepository;
import io.point3.p3api.payment.application.port.Point3PaymentPort;
import io.point3.p3api.payment.application.port.Point3RefundResult;
import io.point3.p3api.payment.application.result.Point3CaptureResult;
import io.point3.p3api.payment.application.result.Point3PaymentSession;
import io.point3.p3api.payment.domain.entity.PaymentAttempt;
import io.point3.p3api.payment.domain.entity.Refund;
import io.point3.p3api.payment.domain.type.PaymentAttemptStatus;
import io.point3.p3api.payment.domain.type.RefundStatus;
import io.point3.p3api.payment.infrastructure.persistence.PaymentAttemptJpaRepository;
import io.point3.p3api.payment.infrastructure.persistence.RefundJpaRepository;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.store.domain.entity.StoreRefundPolicy;
import io.point3.p3api.store.infrastructure.persistence.StoreJpaRepository;
import io.point3.p3api.store.infrastructure.persistence.StoreRefundPolicyJpaRepository;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Import(OrderStateServiceIntegrationTest.RefundTestConfiguration.class)
class OrderStateServiceIntegrationTest extends IntegrationTestSupport {

  private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

  @Autowired
  private OrderQueryUseCase orderQueryUseCase;

  @Autowired
  private OrderStateUseCase orderStateUseCase;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private StoreJpaRepository storeJpaRepository;

  @Autowired
  private StoreRefundPolicyJpaRepository storeRefundPolicyJpaRepository;

  @Autowired
  private InquiryJpaRepository inquiryJpaRepository;

  @Autowired
  private OrderConfirmationJpaRepository orderConfirmationJpaRepository;

  @Autowired
  private PaymentAttemptJpaRepository paymentAttemptJpaRepository;

  @Autowired
  private OrderJpaRepository orderJpaRepository;

  @Autowired
  private RefundJpaRepository refundJpaRepository;

  @Autowired
  private NotificationJpaRepository notificationJpaRepository;

  @Autowired
  private RecordingPoint3PaymentPort point3PaymentPort;

  @BeforeEach
  void resetPoint3PaymentPort() {
    point3PaymentPort.reset();
  }

  @TestConfiguration
  static class RefundTestConfiguration {
    @Bean
    @Primary
    RecordingPoint3PaymentPort point3PaymentPort() {
      return new RecordingPoint3PaymentPort();
    }
  }

  static class RecordingPoint3PaymentPort implements Point3PaymentPort {

    private int refundCallCount;
    private long lastRefundAmount;
    private boolean refundCompleted = true;

    @Override
    public Point3PaymentSession createSession(
        long amount, String productName, String merchantName) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Point3CaptureResult capture(String sessionId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Point3CaptureResult getSession(String sessionId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Point3RefundResult refund(String sessionId, long amount, String reason, String key) {
      refundCallCount++;
      lastRefundAmount = amount;
      return new Point3RefundResult(
          refundCompleted, refundCompleted ? null : "POINT3_REFUND_FAILED");
    }

    void reset() {
      refundCallCount = 0;
      lastRefundAmount = 0;
      refundCompleted = true;
    }
  }

  @Test
  @DisplayName("주문 상세은 결제시도와 환불 내역을 함께 반환한다")
  void getsOrderDetailWithPaymentAndRefunds() {
    Fixture fixture = prepareFixture("order-detail");
    Refund refund = Refund.create(
        fixture.order().getId(),
        fixture.paymentAttempt().getId(),
        fixture.seller().getId(),
        fixture.order().getPaidAmount(),
        "판매자 취소");
    refund.complete(Instant.parse("2026-09-01T01:00:00Z"));
    refundJpaRepository.saveAndFlush(refund);

    OrderDetailResult buyerDetail =
        orderQueryUseCase.getBuyerOrder(fixture.order().getId(), fixture.buyer().getId());
    OrderDetailResult sellerDetail = orderQueryUseCase.getSellerOrder(
        fixture.order().getId(), fixture.store().getId());

    assertEquals(fixture.order().getId(), buyerDetail.order().id());
    assertEquals(fixture.inquiry().getId(), buyerDetail.order().inquiryId());
    assertEquals(PaymentAttemptStatus.SUCCEEDED, buyerDetail.paymentAttempt().status());
    assertEquals(1, buyerDetail.refunds().size());
    assertEquals(RefundStatus.COMPLETED, buyerDetail.refunds().get(0).status());
    assertEquals(fixture.order().getId(), sellerDetail.order().id());
    assertEquals(2, sellerDetail.optionRows().size());
    assertEquals("메뉴명", sellerDetail.optionRows().get(0).label());
    assertEquals("초코 케이크", sellerDetail.optionRows().get(0).value());
    assertEquals("토핑", sellerDetail.optionRows().get(1).label());
    assertEquals("딸기", sellerDetail.optionRows().get(1).value());
  }

  @Test
  @DisplayName("구매자는 주문 취소를 요청하고 판매자는 전체 환불 완료 처리한다")
  void requestsCancelAndRefunds() {
    Fixture fixture = prepareFixture("order-refund");

    OrderResult cancelRequested = orderStateUseCase.requestCancel(
        RequestOrderCancelCommand.of(fixture.order().getId(), fixture.buyer().getId(), "픽업 일정 변경"));
    OrderDetailResult refunded = orderStateUseCase.refund(RefundOrderCommand.of(
        fixture.order().getId(),
        fixture.store().getId(),
        fixture.seller().getId(),
        "구매자 취소 요청 승인"));

    assertEquals(OrderStatus.CANCEL_REQUESTED, cancelRequested.status());
    assertEquals("픽업 일정 변경", cancelRequested.cancelReason());
    assertNotNull(cancelRequested.cancelRequestedAt());
    assertEquals(OrderStatus.REFUNDED, refunded.order().status());
    assertEquals("구매자 취소 요청 승인", refunded.order().cancelReason());
    assertEquals(1, refunded.refunds().size());
    assertEquals(fixture.order().getPaidAmount(), refunded.refunds().get(0).amount());
    assertEquals(RefundStatus.COMPLETED, refunded.refunds().get(0).status());
    assertEquals(
        1,
        notificationJpaRepository
            .findAllByUserIdOrderByCreatedAtDesc(fixture.seller().getId())
            .stream()
            .filter(
                notification -> notification.getType() == NotificationType.ORDER_CANCEL_REQUESTED)
            .count());
    assertEquals(
        1,
        notificationJpaRepository
            .findAllByUserIdOrderByCreatedAtDesc(fixture.buyer().getId())
            .stream()
            .filter(notification -> notification.getType() == NotificationType.ORDER_REFUNDED)
            .count());
  }

  @Test
  @DisplayName("판매자 환불은 사유가 없으면 기본 사유로 처리한다")
  void refundsWithDefaultReason() {
    Fixture fixture = prepareFixture("order-refund-default");

    OrderDetailResult refunded = orderStateUseCase.refund(RefundOrderCommand.of(
        fixture.order().getId(), fixture.store().getId(), fixture.seller().getId(), null));

    assertEquals(OrderStatus.REFUNDED, refunded.order().status());
    assertEquals("판매자 환불 처리", refunded.order().cancelReason());
    assertEquals("판매자 환불 처리", refunded.refunds().getFirst().reason());
  }

  @Test
  @DisplayName("스토어 정책의 환불률로 부분 환불하고 Point3에 같은 금액을 요청한다")
  void refundsPartialAmountFromStorePolicy() {
    Fixture fixture = prepareFixture(
        "order-partial-refund",
        6,
        List.of(new PolicyRule(7, 100), new PolicyRule(5, 80), new PolicyRule(3, 50)));

    OrderDetailResult refunded = orderStateUseCase.refund(RefundOrderCommand.of(
        fixture.order().getId(), fixture.store().getId(), fixture.seller().getId(), "정책 부분 환불"));

    assertEquals(OrderStatus.REFUNDED, refunded.order().status());
    assertEquals(32_800, refunded.refunds().getFirst().amount());
    assertEquals(80, refunded.refunds().getFirst().refundRate());
    assertEquals(1, point3PaymentPort.refundCallCount);
    assertEquals(32_800, point3PaymentPort.lastRefundAmount);
  }

  @Test
  @DisplayName("적용할 환불정책이 없으면 0원으로 완료하고 Point3를 호출하지 않는다")
  void completesZeroAmountRefundWithoutPoint3Request() {
    Fixture fixture = prepareFixture(
        "order-zero-refund",
        2,
        List.of(new PolicyRule(7, 100), new PolicyRule(5, 80), new PolicyRule(3, 50)));

    OrderDetailResult refunded = orderStateUseCase.refund(RefundOrderCommand.of(
        fixture.order().getId(), fixture.store().getId(), fixture.seller().getId(), "환불 가능 기간 경과"));

    assertEquals(OrderStatus.REFUNDED, refunded.order().status());
    assertEquals(0, refunded.refunds().getFirst().amount());
    assertEquals(0, refunded.refunds().getFirst().refundRate());
    assertEquals(RefundStatus.COMPLETED, refunded.refunds().getFirst().status());
    assertEquals(0, point3PaymentPort.refundCallCount);
  }

  @Test
  @DisplayName("완료된 주문 환불은 다시 요청할 수 없다")
  void rejectsDuplicateRefund() {
    Fixture fixture = prepareFixture("order-duplicate-refund");
    RefundOrderCommand command = RefundOrderCommand.of(
        fixture.order().getId(), fixture.store().getId(), fixture.seller().getId(), "중복 환불");
    orderStateUseCase.refund(command);

    BaseException exception =
        assertThrows(BaseException.class, () -> orderStateUseCase.refund(command));

    assertEquals(OrderErrorCode.ORDER_REFUND_ALREADY_PROCESSED, exception.getErrorCode());
    assertEquals(1, point3PaymentPort.refundCallCount);
  }

  @Test
  @DisplayName("Point3 환불 실패는 실패 내역을 남기고 같은 주문의 재시도를 허용한다")
  void recordsFailedRefundAndAllowsRetry() {
    Fixture fixture = prepareFixture("order-refund-retry");
    RefundOrderCommand command = RefundOrderCommand.of(
        fixture.order().getId(), fixture.store().getId(), fixture.seller().getId(), "환불 재시도");
    point3PaymentPort.refundCompleted = false;

    OrderDetailResult failed = orderStateUseCase.refund(command);

    assertEquals(OrderStatus.PAID, failed.order().status());
    assertEquals(RefundStatus.FAILED, failed.refunds().getFirst().status());

    point3PaymentPort.refundCompleted = true;
    OrderDetailResult retried = orderStateUseCase.refund(command);

    assertEquals(OrderStatus.REFUNDED, retried.order().status());
    assertEquals(2, retried.refunds().size());
    assertEquals(
        1,
        retried.refunds().stream()
            .filter(refund -> refund.status() == RefundStatus.COMPLETED)
            .count());
    assertEquals(2, point3PaymentPort.refundCallCount);
  }

  @Test
  @DisplayName("판매자는 결제완료 주문을 픽업완료로 변경한다")
  void picksUpPaidOrder() {
    Fixture fixture = prepareFixture("order-pickup");

    OrderResult pickedUp = orderStateUseCase.pickUp(
        CompleteOrderPickupCommand.of(fixture.order().getId(), fixture.store().getId()));
    Inquiry inquiry = inquiryJpaRepository.findById(fixture.inquiry().getId()).orElseThrow();

    assertEquals(OrderStatus.PICKED_UP, pickedUp.status());
    assertEquals(InquiryStatus.PICKED_UP, inquiry.getStatus());
  }

  @Test
  @DisplayName("허용되지 않은 주문 상태 전이는 차단한다")
  void rejectsInvalidTransitions() {
    Fixture fixture = prepareFixture("order-invalid");
    orderStateUseCase.pickUp(
        CompleteOrderPickupCommand.of(fixture.order().getId(), fixture.store().getId()));

    BaseException buyerException = assertThrows(
        BaseException.class,
        () -> orderStateUseCase.requestCancel(RequestOrderCancelCommand.of(
            fixture.order().getId(), fixture.buyer().getId(), "취소 요청")));
    BaseException sellerException = assertThrows(
        BaseException.class,
        () -> orderStateUseCase.refund(RefundOrderCommand.of(
            fixture.order().getId(), fixture.store().getId(), fixture.seller().getId(), "환불 처리")));

    assertEquals(OrderErrorCode.ORDER_STATUS_FORBIDDEN, buyerException.getErrorCode());
    assertEquals(OrderErrorCode.ORDER_STATUS_FORBIDDEN, sellerException.getErrorCode());
  }

  private Fixture prepareFixture(String prefix) {
    return prepareFixture(prefix, 7, List.of(new PolicyRule(0, 100)));
  }

  private Fixture prepareFixture(
      String prefix, int pickupDaysFromToday, List<PolicyRule> refundPolicies) {
    User seller = saveUser(UserRole.SELLER, prefix + "-seller");
    User buyer = saveUser(UserRole.BUYER, prefix + "-buyer");
    Store store = storeJpaRepository.saveAndFlush(
        Store.create(seller.getId(), "주문 테스트 스토어 " + prefix, "order-test-" + UUID.randomUUID()));
    for (int index = 0; index < refundPolicies.size(); index++) {
      PolicyRule policy = refundPolicies.get(index);
      storeRefundPolicyJpaRepository.save(StoreRefundPolicy.create(
          store.getId(), policy.daysBeforePickup(), policy.refundRate(), index));
    }
    storeRefundPolicyJpaRepository.flush();
    Inquiry inquiry =
        inquiryJpaRepository.saveAndFlush(Inquiry.create(store.getId(), buyer.getId()));
    OrderConfirmation confirmation = orderConfirmationJpaRepository.saveAndFlush(
        createConfirmation(inquiry.getId(), seller.getId(), pickupAt(pickupDaysFromToday)));
    PaymentAttempt paymentAttempt =
        paymentAttemptJpaRepository.saveAndFlush(createPaymentAttempt(confirmation, buyer));
    Order order = orderJpaRepository.saveAndFlush(Order.create(
        store.getId(),
        buyer.getId(),
        inquiry.getId(),
        confirmation.getId(),
        paymentAttempt.getId(),
        "P3-20260901-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
        confirmation.getMenuName(),
        confirmation.getOptionSummary(),
        paymentAttempt.getAmount(),
        confirmation.getPickupAt()));

    return new Fixture(seller, buyer, store, inquiry, confirmation, paymentAttempt, order);
  }

  private OrderConfirmation createConfirmation(
      UUID inquiryId, UUID sellerUserId, Instant pickupAt) {
    OrderConfirmation confirmation = OrderConfirmation.create(
        inquiryId,
        null,
        sellerUserId,
        "초코 케이크 1호",
        "딸기 토핑",
        41000,
        pickupAt,
        "주문 테스트 스토어",
        "{\"answers\":[{\"label\":\"메뉴명\","
            + "\"selectedOptions\":[{\"label\":\"초코 케이크\",\"price\":0}]}]}",
        "[{\"label\":\"토핑\",\"value\":\"딸기\",\"amount\":3000}]",
        "픽업 전 연락");
    confirmation.sent(Instant.parse("2026-08-30T01:00:00Z"));
    confirmation.markPaid();
    return confirmation;
  }

  private Instant pickupAt(int daysFromToday) {
    return LocalDate.now(KOREA_ZONE_ID)
        .plus(daysFromToday, ChronoUnit.DAYS)
        .atTime(LocalTime.NOON)
        .atZone(KOREA_ZONE_ID)
        .toInstant();
  }

  private PaymentAttempt createPaymentAttempt(OrderConfirmation confirmation, User buyer) {
    PaymentAttempt paymentAttempt = PaymentAttempt.create(
        confirmation.getId(),
        buyer.getId(),
        "pymt_sess-" + UUID.randomUUID(),
        null,
        confirmation.getAmount(),
        Instant.parse("2026-09-01T01:00:00Z"));
    paymentAttempt.succeed("payer-" + UUID.randomUUID(), Instant.parse("2026-08-30T02:00:00Z"));
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

  private record Fixture(
      User seller,
      User buyer,
      Store store,
      Inquiry inquiry,
      OrderConfirmation confirmation,
      PaymentAttempt paymentAttempt,
      Order order) {}

  private record PolicyRule(int daysBeforePickup, int refundRate) {}
}
