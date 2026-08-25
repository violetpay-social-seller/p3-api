package io.point3.p3api.payment.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.PaymentErrorCode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.inquiry.application.command.OpenInquiryCommand;
import io.point3.p3api.inquiry.application.open.InquiryOpenService;
import io.point3.p3api.inquiry.application.submission.create.OrderFormSubmissionService;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import io.point3.p3api.order.application.OrderConfirmationService;
import io.point3.p3api.order.application.result.SendOrderConfirmationResult;
import io.point3.p3api.order.application.send.SendOrderConfirmationCommand;
import io.point3.p3api.order.application.state.OrderConfirmationStateService;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
import io.point3.p3api.order.infrastructure.persistence.OrderConfirmationJpaRepository;
import io.point3.p3api.orderform.application.OrderFormService;
import io.point3.p3api.orderform.application.create.CreateOrderFormCommand;
import io.point3.p3api.orderform.application.result.OrderFormResult;
import io.point3.p3api.orderform.domain.type.FieldType;
import io.point3.p3api.payment.application.capture.CapturePaymentCommand;
import io.point3.p3api.payment.application.capture.PaymentCaptureUseCase;
import io.point3.p3api.payment.application.port.Point3PaymentPort;
import io.point3.p3api.payment.application.prepare.PaymentPrepareUseCase;
import io.point3.p3api.payment.application.prepare.PreparePaymentCommand;
import io.point3.p3api.payment.application.result.PaymentCaptureResult;
import io.point3.p3api.payment.application.result.PaymentPreparationResult;
import io.point3.p3api.payment.application.result.Point3CaptureResult;
import io.point3.p3api.payment.application.result.Point3PaymentSession;
import io.point3.p3api.payment.domain.entity.PaymentAttempt;
import io.point3.p3api.payment.domain.type.PaymentAttemptStatus;
import io.point3.p3api.payment.infrastructure.persistence.PaymentAttemptJpaRepository;
import io.point3.p3api.store.application.StoreService;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
    properties = {
      "p3.point3.client-id=test-client",
      "p3.point3.auth-base-url=https://auth.point3.test",
      "p3.point3.payment-origin=https://pay.point3.test"
    })
class PaymentServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private PaymentPrepareUseCase paymentPrepareUseCase;

  @Autowired
  private PaymentCaptureUseCase paymentCaptureUseCase;

  @Autowired
  private OrderConfirmationService orderConfirmationService;

  @Autowired
  private OrderConfirmationStateService orderConfirmationStateService;

  @Autowired
  private OrderFormSubmissionService submissionService;

  @Autowired
  private OrderFormService orderFormService;

  @Autowired
  private InquiryOpenService inquiryOpenService;

  @Autowired
  private StoreService storeService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private PaymentAttemptJpaRepository paymentAttemptJpaRepository;

  @Autowired
  private OrderConfirmationJpaRepository orderConfirmationJpaRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private FakePoint3PaymentPort point3PaymentPort;

  @BeforeEach
  void setUp() {
    point3PaymentPort.clear();
  }

  @Test
  @DisplayName("결제 준비는 구매자 권한과 주문확인서 확인 여부를 검증하고 저장 금액으로 세션을 만든다")
  void preparesWithStoredAmount() {
    Fixture fixture = prepareFixture("payment-prepare");
    SendOrderConfirmationResult confirmation = sendConfirmation(fixture);
    orderConfirmationStateService.markBuyerViewed(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId());

    PaymentPreparationResult result = paymentPrepareUseCase.prepare(PreparePaymentCommand.of(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId()));
    PaymentAttempt paymentAttempt =
        paymentAttemptJpaRepository.findById(result.paymentAttemptId()).orElseThrow();

    assertEquals(41000, result.amount());
    assertEquals(41000, point3PaymentPort.lastAmount());
    assertEquals("/regist", result.entryPath());
    assertFalse(result.authenticationUrl().contains("payer_id="));
    assertEquals(PaymentAttemptStatus.READY, paymentAttempt.getStatus());
    assertEquals(result.sessionId(), paymentAttempt.getPoint3SessionId());
    assertEquals(result.expiresAt(), paymentAttempt.getExpiresAt());
  }

  @Test
  @DisplayName("저장된 payerId가 있으면 결제 준비 응답은 login 진입 값을 반환한다")
  void preparesWithPayerId() {
    Fixture fixture = prepareFixture("payment-login");
    fixture.buyer().connectPayer("payer-saved");
    userJpaRepository.saveAndFlush(fixture.buyer());
    SendOrderConfirmationResult confirmation = sendConfirmation(fixture);
    orderConfirmationStateService.markBuyerViewed(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId());

    PaymentPreparationResult result = paymentPrepareUseCase.prepare(PreparePaymentCommand.of(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId()));

    assertEquals("/login", result.entryPath());
    assertEquals("payer-saved", result.payerId());
    assertEquals("test-client", result.authnClientId());
    assertEquals("https://pay.point3.test", result.point3PaymentOrigin());
    assertEquals(
        "https://auth.point3.test/login?client_id=test-client&session_id="
            + result.sessionId()
            + "&state="
            + result.authnState()
            + "&payer_id=payer-saved",
        result.authenticationUrl());
  }

  @Test
  @DisplayName("주문확인서를 확인하지 않은 구매자는 결제 준비를 할 수 없다")
  void rejectsBeforeViewed() {
    Fixture fixture = prepareFixture("payment-not-viewed");
    SendOrderConfirmationResult confirmation = sendConfirmation(fixture);

    BaseException exception = assertThrows(
        BaseException.class,
        () -> paymentPrepareUseCase.prepare(PreparePaymentCommand.of(
            fixture.inquiry().getId(),
            confirmation.orderConfirmation().id(),
            fixture.buyer().getId())));

    assertEquals(PaymentErrorCode.PAYMENT_CONFIRMATION_NOT_PAYABLE, exception.getErrorCode());
  }

  @Test
  @DisplayName("문의방 구매자가 아니면 결제 준비를 할 수 없다")
  void rejectsOtherBuyerPrepare() {
    Fixture fixture = prepareFixture("payment-other-buyer");
    User otherBuyer = saveUser(UserRole.BUYER, "payment-other-buyer2");
    SendOrderConfirmationResult confirmation = sendConfirmation(fixture);
    orderConfirmationStateService.markBuyerViewed(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId());

    assertThrows(
        BaseException.class,
        () -> paymentPrepareUseCase.prepare(PreparePaymentCommand.of(
            fixture.inquiry().getId(), confirmation.orderConfirmation().id(), otherBuyer.getId())));
    assertEquals(0, point3PaymentPort.createCount());
  }

  @Test
  @DisplayName("결제 승인은 세션을 검증하고 성공 시 주문과 payerId를 저장한다")
  void capturesPaymentAndCreatesOrder() {
    Fixture fixture = prepareFixture("payment-capture");
    SendOrderConfirmationResult confirmation = sendConfirmation(fixture);
    orderConfirmationStateService.markBuyerViewed(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId());
    PaymentPreparationResult prepared = paymentPrepareUseCase.prepare(PreparePaymentCommand.of(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId()));
    point3PaymentPort.nextCaptureStatus(Point3CaptureResult.Status.CAPTURED);

    PaymentCaptureResult captured = paymentCaptureUseCase.capture(CapturePaymentCommand.of(
        prepared.paymentAttemptId(), fixture.buyer().getId(), prepared.sessionId(), "payer-new"));
    PaymentCaptureResult duplicated = paymentCaptureUseCase.capture(CapturePaymentCommand.of(
        prepared.paymentAttemptId(), fixture.buyer().getId(), prepared.sessionId(), "payer-new"));
    User payer = userJpaRepository.findById(fixture.buyer().getId()).orElseThrow();
    OrderConfirmation paidConfirmation = orderConfirmationJpaRepository
        .findById(confirmation.orderConfirmation().id())
        .orElseThrow();

    assertEquals(PaymentAttemptStatus.SUCCEEDED, captured.status());
    assertNotNull(captured.orderId());
    assertEquals(captured.orderId(), duplicated.orderId());
    assertEquals(1, point3PaymentPort.captureCount());
    assertEquals("payer-new", payer.getPayerId());
    assertEquals(OrderConfirmationStatus.PAID, paidConfirmation.getStatus());
  }

  @Test
  @DisplayName("결제 승인 메시지의 세션이 저장된 세션과 다르면 거절한다")
  void rejectsSessionMismatch() {
    Fixture fixture = prepareFixture("payment-mismatch");
    SendOrderConfirmationResult confirmation = sendConfirmation(fixture);
    orderConfirmationStateService.markBuyerViewed(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId());
    PaymentPreparationResult prepared = paymentPrepareUseCase.prepare(PreparePaymentCommand.of(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId()));

    BaseException exception = assertThrows(
        BaseException.class,
        () -> paymentCaptureUseCase.capture(CapturePaymentCommand.of(
            prepared.paymentAttemptId(), fixture.buyer().getId(), "pymt_sess-other", "payer-new")));

    assertEquals(PaymentErrorCode.PAYMENT_SESSION_MISMATCH, exception.getErrorCode());
    assertEquals(0, point3PaymentPort.captureCount());
  }

  @Test
  @DisplayName("Point3 승인 결과가 처리 중이면 결과 확인 필요 상태로 저장하고 중복 승인을 막는다")
  void storesNeedsConfirmation() {
    Fixture fixture = prepareFixture("payment-processing");
    SendOrderConfirmationResult confirmation = sendConfirmation(fixture);
    orderConfirmationStateService.markBuyerViewed(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId());
    PaymentPreparationResult prepared = paymentPrepareUseCase.prepare(PreparePaymentCommand.of(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId()));
    point3PaymentPort.nextCaptureStatus(Point3CaptureResult.Status.PROCESSING);

    PaymentCaptureResult result = paymentCaptureUseCase.capture(CapturePaymentCommand.of(
        prepared.paymentAttemptId(), fixture.buyer().getId(), prepared.sessionId(), "payer-new"));
    PaymentCaptureResult duplicated = paymentCaptureUseCase.capture(CapturePaymentCommand.of(
        prepared.paymentAttemptId(), fixture.buyer().getId(), prepared.sessionId(), "payer-new"));

    assertEquals(PaymentAttemptStatus.NEEDS_CONFIRMATION, result.status());
    assertEquals(PaymentAttemptStatus.NEEDS_CONFIRMATION, duplicated.status());
    assertEquals(1, point3PaymentPort.captureCount());
  }

  private Fixture prepareFixture(String prefix) {
    User seller = saveUser(UserRole.SELLER, prefix + "-seller");
    User buyer = saveUser(UserRole.BUYER, prefix + "-buyer");
    StoreResult store = storeService.create(new CreateStoreCommand(
        seller.getId(),
        "P3 베이커리 " + prefix,
        null,
        "주문제작 케이크 스토어",
        "010-1234-5678",
        true,
        null,
        "{\"mon\":\"10:00-18:00\"}",
        "{\"leadTimeDays\":3}",
        "서울특별시 중구"));
    OrderFormResult form = orderFormService.create(new CreateOrderFormCommand(
        store.id(),
        "주문서 " + prefix,
        List.of(new CreateOrderFormCommand.Field("메뉴명", FieldType.TEXT, true, null, 0))));
    Inquiry inquiry = inquiryOpenService.open(OpenInquiryCommand.of(store.id(), buyer.getId()));
    OrderFormSubmission submission = submitOrderForm(store.id(), buyer.getId(), inquiry, form);

    return new Fixture(seller, buyer, store, form, inquiry, submission);
  }

  private OrderFormSubmission submitOrderForm(
      UUID storeId, UUID buyerUserId, Inquiry inquiry, OrderFormResult form) {
    return submissionService.create(new CreateOrderFormSubmissionCommand(
        storeId,
        buyerUserId,
        inquiry.getId(),
        form.id(),
        List.of(new CreateOrderFormSubmissionCommand.FormAnswer(
            form.fields().get(0).id(), textNode("초코 케이크"))),
        new CreateOrderFormSubmissionCommand.PickupRequest(
            LocalDate.parse("2026-08-30"), LocalTime.parse("13:30")),
        new CreateOrderFormSubmissionCommand.NoticeAgreement(true),
        CreateOrderFormSubmissionCommand.emptyReferenceAssets()));
  }

  private SendOrderConfirmationResult sendConfirmation(Fixture fixture) {
    return orderConfirmationService.send(new SendOrderConfirmationCommand(
        fixture.inquiry().getId(),
        fixture.store().id(),
        fixture.seller().getId(),
        fixture.submission().getId(),
        "초코 케이크 1호",
        "초코 시트, 딸기 토핑",
        41000,
        Instant.parse("2026-08-30T04:30:00Z"),
        List.of(new SendOrderConfirmationCommand.AdditionalItem("토핑", "딸기", 3000L)),
        "픽업 10분 전에 연락 주세요."));
  }

  private User saveUser(UserRole role, String prefix) {
    return userJpaRepository.saveAndFlush(
        User.create(UUID.randomUUID().toString(), uniqueEmail(prefix), prefix, role));
  }

  private JsonNode textNode(String value) {
    return objectMapper.getNodeFactory().textNode(value);
  }

  private record Fixture(
      User seller,
      User buyer,
      StoreResult store,
      OrderFormResult form,
      Inquiry inquiry,
      OrderFormSubmission submission) {}

  @TestConfiguration
  static class Point3PaymentTestConfiguration {

    @Bean
    @Primary
    FakePoint3PaymentPort fakePoint3PaymentPort() {
      return new FakePoint3PaymentPort();
    }
  }

  static class FakePoint3PaymentPort implements Point3PaymentPort {

    private long lastAmount;
    private int createCount;
    private int captureCount;
    private Point3CaptureResult.Status nextCaptureStatus = Point3CaptureResult.Status.CAPTURED;

    @Override
    public Point3PaymentSession createSession(
        long amount, String productName, String displayMerchantName) {
      this.lastAmount = amount;
      createCount++;
      return new Point3PaymentSession("pymt_sess-" + UUID.randomUUID(), amount);
    }

    @Override
    public Point3CaptureResult capture(String sessionId) {
      captureCount++;
      String failureCode =
          nextCaptureStatus == Point3CaptureResult.Status.PROCESSING ? "POINT3_PROCESSING" : null;
      return new Point3CaptureResult(sessionId, nextCaptureStatus, failureCode);
    }

    void nextCaptureStatus(Point3CaptureResult.Status nextCaptureStatus) {
      this.nextCaptureStatus = nextCaptureStatus;
    }

    long lastAmount() {
      return lastAmount;
    }

    int createCount() {
      return createCount;
    }

    int captureCount() {
      return captureCount;
    }

    void clear() {
      lastAmount = 0;
      createCount = 0;
      captureCount = 0;
      nextCaptureStatus = Point3CaptureResult.Status.CAPTURED;
    }
  }
}
