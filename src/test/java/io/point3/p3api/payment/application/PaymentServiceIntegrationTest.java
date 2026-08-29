package io.point3.p3api.payment.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import io.point3.p3api.inquiry.domain.type.InquiryStatus;
import io.point3.p3api.notification.domain.type.NotificationType;
import io.point3.p3api.notification.infrastructure.persistence.NotificationJpaRepository;
import io.point3.p3api.order.application.OrderConfirmationService;
import io.point3.p3api.order.application.result.SendOrderConfirmationResult;
import io.point3.p3api.order.application.send.SendOrderConfirmationCommand;
import io.point3.p3api.order.application.state.OrderConfirmationStateService;
import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
import io.point3.p3api.order.domain.type.OrderStatus;
import io.point3.p3api.order.infrastructure.persistence.OrderConfirmationJpaRepository;
import io.point3.p3api.order.infrastructure.persistence.OrderJpaRepository;
import io.point3.p3api.orderform.application.OrderFormFieldOptionCommand;
import io.point3.p3api.orderform.application.OrderFormService;
import io.point3.p3api.orderform.application.create.CreateOrderFormCommand;
import io.point3.p3api.orderform.application.result.OrderFormResult;
import io.point3.p3api.orderform.domain.type.FieldType;
import io.point3.p3api.payment.application.capture.CapturePaymentCommand;
import io.point3.p3api.payment.application.capture.PaymentCaptureUseCase;
import io.point3.p3api.payment.application.port.Point3PaymentPort;
import io.point3.p3api.payment.application.prepare.PaymentPrepareUseCase;
import io.point3.p3api.payment.application.prepare.PreparePaymentCommand;
import io.point3.p3api.payment.application.query.PaymentAttemptHistoryQueryUseCase;
import io.point3.p3api.payment.application.query.PaymentCtaQueryUseCase;
import io.point3.p3api.payment.application.result.PaymentAttemptResult;
import io.point3.p3api.payment.application.result.PaymentCaptureResult;
import io.point3.p3api.payment.application.result.PaymentCtaResult;
import io.point3.p3api.payment.application.result.PaymentCtaStatus;
import io.point3.p3api.payment.application.result.PaymentPreparationResult;
import io.point3.p3api.payment.application.result.Point3CaptureResult;
import io.point3.p3api.payment.application.result.Point3PaymentSession;
import io.point3.p3api.payment.domain.entity.PaymentAttempt;
import io.point3.p3api.payment.domain.type.PaymentAttemptStatus;
import io.point3.p3api.payment.infrastructure.persistence.PaymentAttemptJpaRepository;
import io.point3.p3api.store.application.StoreService;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.store.application.setting.StoreSettingService;
import io.point3.p3api.store.application.setting.command.UpdateStoreSettingCommand;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
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

  private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

  @Autowired
  private PaymentPrepareUseCase paymentPrepareUseCase;

  @Autowired
  private PaymentCaptureUseCase paymentCaptureUseCase;

  @Autowired
  private PaymentCtaQueryUseCase paymentCtaQueryUseCase;

  @Autowired
  private PaymentAttemptHistoryQueryUseCase paymentAttemptHistoryQueryUseCase;

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
  private StoreSettingService storeSettingService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private PaymentAttemptJpaRepository paymentAttemptJpaRepository;

  @Autowired
  private OrderConfirmationJpaRepository orderConfirmationJpaRepository;

  @Autowired
  private OrderJpaRepository orderJpaRepository;

  @Autowired
  private NotificationJpaRepository notificationJpaRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private FakePoint3PaymentPort point3PaymentPort;

  @BeforeEach
  void setUp() {
    point3PaymentPort.clear();
  }

  @Test
  @DisplayName("주문확인서 기준 CTA는 확인 전 비활성이고 확인 후 결제 가능 상태를 반환한다")
  void createsPaymentCtaFromConfirmation() {
    Fixture fixture = prepareFixture("payment-cta");
    SendOrderConfirmationResult confirmation = sendConfirmation(fixture);

    PaymentCtaResult beforeViewed = paymentCtaQueryUseCase.getBuyerConfirmationCta(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId());
    orderConfirmationStateService.markBuyerViewed(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId());
    PaymentCtaResult afterViewed = paymentCtaQueryUseCase.getBuyerConfirmationCta(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId());

    assertFalse(beforeViewed.canPay());
    assertEquals(PaymentCtaStatus.VIEW_REQUIRED, beforeViewed.status());
    assertEquals("ORDER_CONFIRMATION_NOT_VIEWED", beforeViewed.reason());
    assertEquals(41000, beforeViewed.amount());
    assertTrue(afterViewed.canPay());
    assertEquals(PaymentCtaStatus.PAYABLE, afterViewed.status());
    assertNotNull(afterViewed.buyerViewedAt());
  }

  @Test
  @DisplayName("결제 실패 후 재시도하면 같은 주문확인서 기준으로 새 결제 세션과 이력을 만든다")
  void retriesWithNewSessionAfterFailure() {
    Fixture fixture = prepareFixture("payment-retry");
    SendOrderConfirmationResult confirmation = sendConfirmation(fixture);
    orderConfirmationStateService.markBuyerViewed(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId());
    PaymentPreparationResult first = paymentPrepareUseCase.prepare(PreparePaymentCommand.of(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId()));
    point3PaymentPort.nextCaptureStatus(Point3CaptureResult.Status.FAILED);
    PaymentCaptureResult failed = paymentCaptureUseCase.capture(CapturePaymentCommand.of(
        first.paymentAttemptId(), fixture.buyer().getId(), first.sessionId(), "payer-new"));

    PaymentCtaResult retryCta = paymentCtaQueryUseCase.getBuyerConfirmationCta(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId());
    point3PaymentPort.nextCaptureStatus(Point3CaptureResult.Status.CAPTURED);
    PaymentPreparationResult retry = paymentPrepareUseCase.prepare(PreparePaymentCommand.of(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId()));
    List<PaymentAttemptResult> attempts = paymentAttemptHistoryQueryUseCase.getBuyerPaymentAttempts(
        fixture.inquiry().getId(),
        confirmation.orderConfirmation().id(),
        fixture.buyer().getId());

    assertEquals(PaymentAttemptStatus.FAILED, failed.status());
    assertTrue(
        orderJpaRepository.findByPaymentAttemptId(first.paymentAttemptId()).isEmpty());
    assertTrue(retryCta.canPay());
    assertEquals(PaymentCtaStatus.RETRY_AVAILABLE, retryCta.status());
    assertEquals(first.paymentAttemptId(), retryCta.latestPaymentAttempt().paymentAttemptId());
    assertNotEquals(first.paymentAttemptId(), retry.paymentAttemptId());
    assertNotEquals(first.sessionId(), retry.sessionId());
    assertEquals(2, attempts.size());
    assertEquals(retry.paymentAttemptId(), attempts.get(0).paymentAttemptId());
    assertEquals(PaymentAttemptStatus.READY, attempts.get(0).status());
    assertEquals(first.paymentAttemptId(), attempts.get(1).paymentAttemptId());
    assertEquals(PaymentAttemptStatus.FAILED, attempts.get(1).status());
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

    Instant expiryFloor = Instant.now().plus(Duration.ofHours(23));
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
    assertTrue(result.expiresAt().isAfter(expiryFloor));
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
    List<Order> orders = orderJpaRepository.findAllByBuyerUserIdOrderByCreatedAtDesc(
        fixture.buyer().getId());
    Order order = orders.get(0);

    assertEquals(PaymentAttemptStatus.SUCCEEDED, captured.status());
    assertNotNull(captured.orderId());
    assertEquals(captured.orderId(), duplicated.orderId());
    assertEquals(1, point3PaymentPort.captureCount());
    assertEquals(1, orders.size());
    assertEquals(captured.orderId(), order.getId());
    assertEquals(fixture.store().id(), order.getStoreId());
    assertEquals(fixture.buyer().getId(), order.getBuyerUserId());
    assertEquals(fixture.inquiry().getId(), order.getInquiryId());
    assertEquals(confirmation.orderConfirmation().id(), order.getConfirmationId());
    assertEquals(prepared.paymentAttemptId(), order.getPaymentAttemptId());
    assertTrue(order.getOrderNumber().matches("P3-\\d{8}-[0-9a-f]{27}"));
    assertEquals(fixture.form().name(), order.getMenuNameSnapshot());
    assertEquals("초코 시트, 딸기 토핑", order.getOptionSummarySnapshot());
    assertEquals(41000, order.getPaidAmount());
    assertEquals(pickupAt(), order.getPickupAt());
    assertEquals(OrderStatus.PAID, order.getStatus());
    assertEquals("payer-new", payer.getPayerId());
    assertEquals(
        1,
        notificationJpaRepository
            .findAllByUserIdOrderByCreatedAtDesc(fixture.buyer().getId())
            .stream()
            .filter(notification -> notification.getType() == NotificationType.PAYMENT_COMPLETED)
            .count());
    assertEquals(
        1,
        notificationJpaRepository
            .findAllByUserIdOrderByCreatedAtDesc(fixture.seller().getId())
            .stream()
            .filter(notification -> notification.getType() == NotificationType.PAYMENT_COMPLETED)
            .count());
    assertEquals(OrderConfirmationStatus.PAID, paidConfirmation.getStatus());
    assertEquals(InquiryStatus.PAID, fixture.inquiry().getStatus());
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
        List.of(
            new CreateOrderFormCommand.Field("메뉴명", FieldType.TEXT, true, null, 0),
            new CreateOrderFormCommand.Field(
                "사이즈",
                FieldType.SINGLE_SELECT,
                true,
                null,
                1,
                "기본 정보",
                null,
                0,
                List.of(new OrderFormFieldOptionCommand("10호", "38000", true, 0))))));
    savePickupSettings(store.id());
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
        List.of(
            new CreateOrderFormSubmissionCommand.FormAnswer(
                form.fields().get(0).id(), textNode("초코 케이크")),
            new CreateOrderFormSubmissionCommand.FormAnswer(
                form.fields().get(1).id(), textNode("38000"))),
        new CreateOrderFormSubmissionCommand.PickupRequest(
            availablePickupDate(), LocalTime.parse("13:30")),
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
        pickupAt(),
        List.of(new SendOrderConfirmationCommand.AdditionalItem("토핑", "딸기", 3000L)),
        "픽업 10분 전에 연락 주세요."));
  }

  private LocalDate availablePickupDate() {
    return LocalDate.now(KOREA_ZONE)
        .plusDays(7)
        .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
  }

  private Instant pickupAt() {
    return availablePickupDate()
        .atTime(LocalTime.parse("13:30"))
        .atZone(KOREA_ZONE)
        .toInstant();
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

  private JsonNode textNode(String value) {
    return objectMapper.getNodeFactory().textNode(value);
  }

  private void savePickupSettings(UUID storeId) {
    storeSettingService.update(new UpdateStoreSettingCommand(
        storeId,
        0,
        "주문 전 공지",
        0,
        java.util.Arrays.stream(DayOfWeek.values())
            .map(day -> new UpdateStoreSettingCommand.WeeklyPickupSetting(
                day, LocalTime.of(10, 0), LocalTime.of(18, 0), 10, true))
            .toList(),
        List.of()));
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
          switch (nextCaptureStatus) {
            case CAPTURED -> null;
            case FAILED -> "POINT3_FAILED";
            case PROCESSING -> "POINT3_PROCESSING";
          };
      return new Point3CaptureResult(sessionId, nextCaptureStatus, failureCode);
    }

    @Override
    public io.point3.p3api.payment.application.port.Point3RefundResult refund(
        String sessionId, long amount, String reason, String idempotencyKey) {
      return new io.point3.p3api.payment.application.port.Point3RefundResult(true, null);
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
