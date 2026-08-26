package io.point3.p3api.operator.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.asset.infrastructure.persistence.AssetJpaRepository;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.gallery.domain.entity.StoreGalleryItem;
import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import io.point3.p3api.gallery.infrastructure.persistence.GalleryItemJpaRepository;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.infrastructure.persistence.InquiryJpaRepository;
import io.point3.p3api.operator.application.command.AnswerServiceInquiryCommand;
import io.point3.p3api.operator.application.command.ChangeGalleryItemStatusCommand;
import io.point3.p3api.operator.application.command.ChangeStoreStatusCommand;
import io.point3.p3api.operator.application.command.ChangeUserStatusCommand;
import io.point3.p3api.operator.application.command.CreateReportCommand;
import io.point3.p3api.operator.application.command.CreateServiceInquiryCommand;
import io.point3.p3api.operator.application.command.ResolveReportCommand;
import io.point3.p3api.operator.application.query.OperatorActionLogQuery;
import io.point3.p3api.operator.application.query.OperatorDashboardQuery;
import io.point3.p3api.operator.application.query.OperatorGalleryItemQuery;
import io.point3.p3api.operator.application.query.OperatorOrderQuery;
import io.point3.p3api.operator.application.query.OperatorPageQuery;
import io.point3.p3api.operator.application.query.OperatorPaymentQuery;
import io.point3.p3api.operator.application.query.OperatorUserQuery;
import io.point3.p3api.operator.application.result.OperatorDashboardResult;
import io.point3.p3api.operator.application.result.OperatorGalleryItemResult;
import io.point3.p3api.operator.application.result.OperatorReportResult;
import io.point3.p3api.operator.application.result.OperatorServiceInquiryResult;
import io.point3.p3api.operator.application.result.OperatorUserResult;
import io.point3.p3api.operator.application.result.PageResult;
import io.point3.p3api.operator.domain.type.OperatorActionType;
import io.point3.p3api.operator.domain.type.OperatorTargetType;
import io.point3.p3api.operator.domain.type.ReportStatus;
import io.point3.p3api.operator.domain.type.ReportTargetAction;
import io.point3.p3api.operator.domain.type.ReportTargetType;
import io.point3.p3api.operator.domain.type.ServiceInquiryStatus;
import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.entity.OrderStatusHistory;
import io.point3.p3api.order.domain.type.OrderStatus;
import io.point3.p3api.order.infrastructure.persistence.OrderConfirmationJpaRepository;
import io.point3.p3api.order.infrastructure.persistence.OrderJpaRepository;
import io.point3.p3api.order.infrastructure.persistence.OrderStatusHistoryJpaRepository;
import io.point3.p3api.payment.domain.entity.PaymentAttempt;
import io.point3.p3api.payment.infrastructure.persistence.PaymentAttemptJpaRepository;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.store.domain.type.StoreStatus;
import io.point3.p3api.store.infrastructure.persistence.StoreJpaRepository;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.domain.type.UserStatus;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OperatorManagementServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private OperatorManagementUseCase operatorManagementUseCase;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private AssetJpaRepository assetJpaRepository;

  @Autowired
  private StoreJpaRepository storeJpaRepository;

  @Autowired
  private GalleryItemJpaRepository galleryItemJpaRepository;

  @Autowired
  private OrderJpaRepository orderJpaRepository;

  @Autowired
  private OrderConfirmationJpaRepository orderConfirmationJpaRepository;

  @Autowired
  private OrderStatusHistoryJpaRepository orderStatusHistoryJpaRepository;

  @Autowired
  private InquiryJpaRepository inquiryJpaRepository;

  @Autowired
  private PaymentAttemptJpaRepository paymentAttemptJpaRepository;

  @Test
  @DisplayName("운영자는 사용자를 차단하고 차단 사용자를 필터 조회한다")
  void changesUserStatus() {
    User operator = saveUser(UserRole.OPERATOR, "operator-user-status");
    User buyer = saveUser(UserRole.BUYER, "buyer-user-status");

    OperatorUserResult changed = operatorManagementUseCase.changeUserStatus(
        ChangeUserStatusCommand.of(buyer.getId(), operator.getId(), UserStatus.BANNED, "abuse"));
    PageResult<OperatorUserResult> bannedUsers = operatorManagementUseCase.getUsers(
        new OperatorUserQuery(null, null, UserStatus.BANNED, new OperatorPageQuery(0, 20)));

    assertEquals(UserStatus.BANNED, changed.status());
    assertEquals(1, bannedUsers.totalElements());
    assertEquals(
        1,
        operatorManagementUseCase
            .getActionLogs(new OperatorActionLogQuery(
                operator.getId(),
                OperatorActionType.USER_STATUS_CHANGED,
                OperatorTargetType.USER,
                buyer.getId(),
                null,
                null,
                new OperatorPageQuery(0, 20)))
            .totalElements());
  }

  @Test
  @DisplayName("운영자는 자기 자신을 차단할 수 없다")
  void rejectsChangingOwnStatus() {
    User operator = saveUser(UserRole.OPERATOR, "operator-own-status");

    BaseException exception = assertThrows(
        BaseException.class,
        () -> operatorManagementUseCase.changeUserStatus(ChangeUserStatusCommand.of(
            operator.getId(), operator.getId(), UserStatus.BANNED, "mistake")));

    assertEquals("COMMON_INVALID_INPUT_400", exception.getErrorCode().getCode());
  }

  @Test
  @DisplayName("운영자는 스토어와 갤러리 이미지 노출 상태를 조치한다")
  void changesStoreAndGalleryStatus() {
    User operator = saveUser(UserRole.OPERATOR, "operator-content-status");
    User seller = saveUser(UserRole.SELLER, "seller-content-status");
    Store store = saveActiveStore(seller.getId(), "operator-content-store");
    Asset asset = saveAsset(seller.getId(), "gallery");
    StoreGalleryItem item =
        galleryItemJpaRepository.save(StoreGalleryItem.create(store.getId(), asset.getId(), 0));

    operatorManagementUseCase.changeStoreStatus(ChangeStoreStatusCommand.of(
        store.getId(), operator.getId(), StoreStatus.SUSPENDED, "policy"));
    operatorManagementUseCase.changeGalleryItemStatus(ChangeGalleryItemStatusCommand.of(
        item.getId(), operator.getId(), StoreGalleryItemStatus.VISIBLE, "restore"));
    PageResult<OperatorGalleryItemResult> galleryItems =
        operatorManagementUseCase.getGalleryItems(new OperatorGalleryItemQuery(
            store.getId(),
            asset.getId(),
            StoreGalleryItemStatus.VISIBLE,
            new OperatorPageQuery(0, 20)));

    assertEquals(
        StoreStatus.SUSPENDED,
        storeJpaRepository.findById(store.getId()).orElseThrow().getStatus());
    assertEquals(
        StoreGalleryItemStatus.VISIBLE,
        galleryItemJpaRepository.findById(item.getId()).orElseThrow().getStatus());
    assertEquals(1, galleryItems.totalElements());
  }

  @Test
  @DisplayName("신고 처리 시 대상 사용자 차단 조치를 함께 수행한다")
  void resolvesReportWithTargetAction() {
    User operator = saveUser(UserRole.OPERATOR, "operator-report-action");
    User reporter = saveUser(UserRole.BUYER, "reporter-report-action");
    User target = saveUser(UserRole.BUYER, "target-report-action");
    OperatorReportResult report = operatorManagementUseCase.createReport(CreateReportCommand.of(
        reporter.getId(), ReportTargetType.USER, target.getId(), "spam", "evidence"));

    OperatorReportResult resolved = operatorManagementUseCase.resolveReport(ResolveReportCommand.of(
        report.id(), operator.getId(), ReportStatus.RESOLVED, "blocked", ReportTargetAction.BAN));

    assertEquals(ReportStatus.RESOLVED, resolved.status());
    assertEquals(
        UserStatus.BANNED,
        userJpaRepository.findById(target.getId()).orElseThrow().getStatus());
  }

  @Test
  @DisplayName("서비스 문의는 접수 후 운영자 답변으로 상태가 변경된다")
  void answersServiceInquiry() {
    User operator = saveUser(UserRole.OPERATOR, "operator-support-answer");
    User buyer = saveUser(UserRole.BUYER, "buyer-support-answer");
    OperatorServiceInquiryResult inquiry = operatorManagementUseCase.createServiceInquiry(
        CreateServiceInquiryCommand.of(buyer.getId(), "help", "body"));

    OperatorServiceInquiryResult answered = operatorManagementUseCase.answerServiceInquiry(
        AnswerServiceInquiryCommand.of(inquiry.id(), operator.getId(), "answer"));

    assertEquals(ServiceInquiryStatus.ANSWERED, answered.status());
    assertEquals(operator.getId(), answered.assigneeOperatorId());
  }

  @Test
  @DisplayName("운영자는 주문 상태 이력을 조회한다")
  void getsOrderHistories() {
    User operator = saveUser(UserRole.OPERATOR, "operator-order-history");
    User seller = saveUser(UserRole.SELLER, "seller-order-history");
    User buyer = saveUser(UserRole.BUYER, "buyer-order-history");
    Store store = saveActiveStore(seller.getId(), "operator-order-history-store");
    Inquiry inquiry = inquiryJpaRepository.save(Inquiry.create(store.getId(), buyer.getId()));
    OrderConfirmation confirmation = orderConfirmationJpaRepository.save(OrderConfirmation.create(
        inquiry.getId(),
        null,
        seller.getId(),
        "cake",
        "option",
        30000,
        Instant.now(),
        store.getName(),
        null,
        null,
        null));
    PaymentAttempt paymentAttempt = paymentAttemptJpaRepository.save(PaymentAttempt.create(
        confirmation.getId(),
        buyer.getId(),
        "session-" + UUID.randomUUID(),
        null,
        30000,
        Instant.now().plusSeconds(3600)));
    Order order = orderJpaRepository.save(Order.create(
        store.getId(),
        buyer.getId(),
        inquiry.getId(),
        confirmation.getId(),
        paymentAttempt.getId(),
        "P3-" + UUID.randomUUID(),
        "cake",
        "option",
        30000,
        Instant.now()));
    orderStatusHistoryJpaRepository.save(OrderStatusHistory.create(
        order.getId(), null, OrderStatus.PAID, buyer.getId(), "PAYMENT_SUCCEEDED", Instant.now()));

    assertEquals(1, operatorManagementUseCase.getOrderHistories(order.getId()).size());
    assertTrue(operatorManagementUseCase
        .exportOrdersCsv(new OperatorOrderQuery(
            store.getId(), buyer.getId(), OrderStatus.PAID, null, null, null))
        .contains(order.getOrderNumber()));
    assertTrue(operatorManagementUseCase
        .exportPaymentsCsv(new OperatorPaymentQuery(null, null, null, null))
        .contains(paymentAttempt.getId().toString()));

    OperatorDashboardResult dashboard = operatorManagementUseCase.getDashboard(
        new OperatorDashboardQuery(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)));
    assertEquals(1, dashboard.totalStores());
    assertEquals(UserRole.OPERATOR, operator.getRole());
  }

  private User saveUser(UserRole role, String prefix) {
    return userJpaRepository.save(
        User.create("cognito-" + UUID.randomUUID(), uniqueEmail(prefix), prefix, role));
  }

  private Store saveActiveStore(UUID ownerUserId, String name) {
    Store store = Store.create(ownerUserId, name, name + "-" + UUID.randomUUID());
    store.active();
    return storeJpaRepository.save(store);
  }

  private Asset saveAsset(UUID uploadedBy, String prefix) {
    UUID assetId = UUID.randomUUID();
    return assetJpaRepository.save(Asset.create(
        assetId,
        uploadedBy,
        prefix + ".png",
        "image/png",
        100,
        "operator-test/" + assetId + ".png"));
  }
}
