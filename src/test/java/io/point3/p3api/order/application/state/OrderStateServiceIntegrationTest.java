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
import io.point3.p3api.order.application.query.order.OrderQueryUseCase;
import io.point3.p3api.order.application.result.OrderDetailResult;
import io.point3.p3api.order.application.result.OrderResult;
import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderStatus;
import io.point3.p3api.order.infrastructure.persistence.OrderConfirmationJpaRepository;
import io.point3.p3api.order.infrastructure.persistence.OrderJpaRepository;
import io.point3.p3api.payment.domain.entity.PaymentAttempt;
import io.point3.p3api.payment.domain.entity.Refund;
import io.point3.p3api.payment.domain.type.PaymentAttemptStatus;
import io.point3.p3api.payment.domain.type.RefundStatus;
import io.point3.p3api.payment.infrastructure.persistence.PaymentAttemptJpaRepository;
import io.point3.p3api.payment.infrastructure.persistence.RefundJpaRepository;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.store.infrastructure.persistence.StoreJpaRepository;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OrderStateServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private OrderQueryUseCase orderQueryUseCase;

  @Autowired
  private OrderStateUseCase orderStateUseCase;

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

  @Autowired
  private RefundJpaRepository refundJpaRepository;

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
    User seller = saveUser(UserRole.SELLER, prefix + "-seller");
    User buyer = saveUser(UserRole.BUYER, prefix + "-buyer");
    Store store = storeJpaRepository.saveAndFlush(
        Store.create(seller.getId(), "주문 테스트 스토어 " + prefix, "order-test-" + UUID.randomUUID()));
    Inquiry inquiry =
        inquiryJpaRepository.saveAndFlush(Inquiry.create(store.getId(), buyer.getId()));
    OrderConfirmation confirmation = orderConfirmationJpaRepository.saveAndFlush(
        createConfirmation(inquiry.getId(), seller.getId()));
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

  private OrderConfirmation createConfirmation(UUID inquiryId, UUID sellerUserId) {
    OrderConfirmation confirmation = OrderConfirmation.create(
        inquiryId,
        null,
        sellerUserId,
        "초코 케이크 1호",
        "딸기 토핑",
        41000,
        Instant.parse("2026-09-01T04:00:00Z"),
        "주문 테스트 스토어",
        null,
        null,
        "픽업 전 연락");
    confirmation.sent(Instant.parse("2026-08-30T01:00:00Z"));
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
        Instant.parse("2026-09-01T01:00:00Z"));
    paymentAttempt.succeed("payer-" + UUID.randomUUID(), Instant.parse("2026-08-30T02:00:00Z"));
    return paymentAttempt;
  }

  private User saveUser(UserRole role, String prefix) {
    return userJpaRepository.saveAndFlush(
        User.create(UUID.randomUUID().toString(), uniqueEmail(prefix), prefix, role));
  }

  private record Fixture(
      User seller,
      User buyer,
      Store store,
      Inquiry inquiry,
      OrderConfirmation confirmation,
      PaymentAttempt paymentAttempt,
      Order order) {}
}
