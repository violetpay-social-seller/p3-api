package io.point3.p3api.operator.application;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.gallery.domain.entity.StoreGalleryItem;
import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import io.point3.p3api.operator.application.command.AnswerServiceInquiryCommand;
import io.point3.p3api.operator.application.command.AssignServiceInquiryCommand;
import io.point3.p3api.operator.application.command.ChangeGalleryItemStatusCommand;
import io.point3.p3api.operator.application.command.ChangeStoreStatusCommand;
import io.point3.p3api.operator.application.command.ChangeUserStatusCommand;
import io.point3.p3api.operator.application.command.CloseServiceInquiryCommand;
import io.point3.p3api.operator.application.command.CreateReportCommand;
import io.point3.p3api.operator.application.command.CreateServiceInquiryCommand;
import io.point3.p3api.operator.application.command.HoldSellerOnboardingCommand;
import io.point3.p3api.operator.application.command.ResolveReportCommand;
import io.point3.p3api.operator.application.command.ReviewReportCommand;
import io.point3.p3api.operator.application.port.OperatorPersistencePort;
import io.point3.p3api.operator.application.query.OperatorActionLogQuery;
import io.point3.p3api.operator.application.query.OperatorDashboardQuery;
import io.point3.p3api.operator.application.query.OperatorGalleryItemQuery;
import io.point3.p3api.operator.application.query.OperatorOnboardingQuery;
import io.point3.p3api.operator.application.query.OperatorOrderQuery;
import io.point3.p3api.operator.application.query.OperatorPaymentQuery;
import io.point3.p3api.operator.application.query.OperatorRefundQuery;
import io.point3.p3api.operator.application.query.OperatorReportQuery;
import io.point3.p3api.operator.application.query.OperatorServiceInquiryQuery;
import io.point3.p3api.operator.application.query.OperatorStoreQuery;
import io.point3.p3api.operator.application.query.OperatorUserQuery;
import io.point3.p3api.operator.application.result.OperatorActionLogResult;
import io.point3.p3api.operator.application.result.OperatorDashboardResult;
import io.point3.p3api.operator.application.result.OperatorGalleryItemResult;
import io.point3.p3api.operator.application.result.OperatorOnboardingResult;
import io.point3.p3api.operator.application.result.OperatorOrderResult;
import io.point3.p3api.operator.application.result.OperatorPaymentAttemptResult;
import io.point3.p3api.operator.application.result.OperatorRefundResult;
import io.point3.p3api.operator.application.result.OperatorReportResult;
import io.point3.p3api.operator.application.result.OperatorServiceInquiryResult;
import io.point3.p3api.operator.application.result.OperatorStoreResult;
import io.point3.p3api.operator.application.result.OperatorUserResult;
import io.point3.p3api.operator.application.result.OrderStatusHistoryResult;
import io.point3.p3api.operator.application.result.PageResult;
import io.point3.p3api.operator.domain.entity.OperatorActionLog;
import io.point3.p3api.operator.domain.entity.Report;
import io.point3.p3api.operator.domain.entity.ServiceInquiry;
import io.point3.p3api.operator.domain.type.OperatorActionType;
import io.point3.p3api.operator.domain.type.OperatorTargetType;
import io.point3.p3api.operator.domain.type.ReportTargetAction;
import io.point3.p3api.operator.domain.type.ReportTargetType;
import io.point3.p3api.order.application.port.OrderStatusHistoryPersistencePort;
import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.store.domain.type.StoreStatus;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.UserStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OperatorManagementService implements OperatorManagementUseCase {

  private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
  private static final Pageable EXPORT_PAGEABLE =
      PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "createdAt"));

  private final OperatorPersistencePort operatorPersistencePort;
  private final OrderStatusHistoryPersistencePort orderStatusHistoryPersistencePort;
  private final Clock clock;

  @Override
  @Transactional(readOnly = true)
  public OperatorDashboardResult getDashboard(OperatorDashboardQuery query) {
    LocalDate endDate =
        query.endDate() == null ? LocalDate.now(clock.withZone(KOREA_ZONE)) : query.endDate();
    LocalDate startDate = query.startDate() == null ? endDate.minusDays(29) : query.startDate();
    if (startDate.isAfter(endDate)) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT, "startDate must not be after endDate");
    }

    Instant startInclusive = toInstant(startDate);
    Instant endExclusive = toInstant(endDate.plusDays(1));
    long paymentAmount =
        operatorPersistencePort.sumSucceededPaymentAmount(startInclusive, endExclusive);
    long refundAmount =
        operatorPersistencePort.sumCompletedRefundAmount(startInclusive, endExclusive);

    return new OperatorDashboardResult(
        operatorPersistencePort.countUsers(),
        operatorPersistencePort.countUsersByRole(io.point3.p3api.user.domain.type.UserRole.SELLER),
        operatorPersistencePort.countStores(),
        operatorPersistencePort.countPendingOnboardings(),
        operatorPersistencePort.countOpenReports(),
        operatorPersistencePort.countOpenServiceInquiries(),
        startDate,
        endDate,
        paymentAmount,
        refundAmount,
        paymentAmount - refundAmount,
        operatorPersistencePort.countOrdersByStatus(),
        operatorPersistencePort.countPaymentsByStatus(),
        operatorPersistencePort.countRefundsByStatus());
  }

  @Override
  @Transactional(readOnly = true)
  public PageResult<OperatorUserResult> getUsers(OperatorUserQuery query) {
    return PageResult.from(operatorPersistencePort
        .findUsers(
            query.keyword(), query.role(), query.status(), query.pageQuery().toPageable())
        .map(OperatorUserResult::from));
  }

  @Override
  @Transactional(readOnly = true)
  public OperatorUserResult getUser(UUID userId) {
    return OperatorUserResult.from(findUser(userId));
  }

  @Override
  public OperatorUserResult changeUserStatus(ChangeUserStatusCommand command) {
    validateReason(command.reason());
    if (command.targetUserId().equals(command.operatorUserId())) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT, "Operator can not change own status");
    }

    User user = findUser(command.targetUserId());
    changeUserStatus(user, command.status());
    operatorPersistencePort.saveActionLog(OperatorActionLog.create(
        command.operatorUserId(),
        OperatorActionType.USER_STATUS_CHANGED,
        OperatorTargetType.USER,
        user.getId(),
        command.reason()));
    return OperatorUserResult.from(operatorPersistencePort.saveUser(user));
  }

  @Override
  @Transactional(readOnly = true)
  public PageResult<OperatorStoreResult> getStores(OperatorStoreQuery query) {
    return PageResult.from(operatorPersistencePort
        .findStores(query.keyword(), query.status(), query.pageQuery().toPageable())
        .map(OperatorStoreResult::from));
  }

  @Override
  @Transactional(readOnly = true)
  public OperatorStoreResult getStore(UUID storeId) {
    return OperatorStoreResult.from(findStore(storeId));
  }

  @Override
  public OperatorStoreResult changeStoreStatus(ChangeStoreStatusCommand command) {
    validateReason(command.reason());
    Store store = findStore(command.storeId());
    changeStoreStatus(store, command.status());
    operatorPersistencePort.saveActionLog(OperatorActionLog.create(
        command.operatorUserId(),
        OperatorActionType.STORE_STATUS_CHANGED,
        OperatorTargetType.STORE,
        store.getId(),
        command.reason()));
    return OperatorStoreResult.from(operatorPersistencePort.saveStore(store));
  }

  @Override
  @Transactional(readOnly = true)
  public PageResult<OperatorGalleryItemResult> getGalleryItems(OperatorGalleryItemQuery query) {
    return PageResult.from(operatorPersistencePort
        .findGalleryItems(
            query.storeId(), query.assetId(), query.status(), query.pageQuery().toPageable())
        .map(OperatorGalleryItemResult::from));
  }

  @Override
  @Transactional(readOnly = true)
  public OperatorGalleryItemResult getGalleryItem(UUID galleryItemId) {
    return OperatorGalleryItemResult.from(findGalleryItem(galleryItemId));
  }

  @Override
  public OperatorGalleryItemResult changeGalleryItemStatus(ChangeGalleryItemStatusCommand command) {
    validateReason(command.reason());
    StoreGalleryItem item = findGalleryItem(command.galleryItemId());
    if (command.status() == StoreGalleryItemStatus.VISIBLE) {
      item.show();
    } else {
      item.hide();
    }
    operatorPersistencePort.saveActionLog(OperatorActionLog.create(
        command.operatorUserId(),
        OperatorActionType.GALLERY_ITEM_STATUS_CHANGED,
        OperatorTargetType.GALLERY_ITEM,
        item.getId(),
        command.reason()));
    return OperatorGalleryItemResult.from(operatorPersistencePort.saveGalleryItem(item));
  }

  @Override
  @Transactional(readOnly = true)
  public PageResult<OperatorOnboardingResult> getOnboardings(OperatorOnboardingQuery query) {
    return PageResult.from(operatorPersistencePort
        .findOnboardings(query.status(), query.pageQuery().toPageable())
        .map(OperatorOnboardingResult::from));
  }

  @Override
  @Transactional(readOnly = true)
  public OperatorOnboardingResult getOnboarding(UUID onboardingId) {
    return OperatorOnboardingResult.from(findOnboarding(onboardingId));
  }

  @Override
  public OperatorOnboardingResult holdOnboarding(HoldSellerOnboardingCommand command) {
    validateReason(command.reason());
    SellerOnboarding onboarding = findOnboarding(command.onboardingId());
    try {
      onboarding.hold(command.operatorUserId(), command.reason(), Instant.now(clock));
    } catch (IllegalStateException e) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT, e.getMessage());
    }
    operatorPersistencePort.saveActionLog(OperatorActionLog.create(
        command.operatorUserId(),
        OperatorActionType.SELLER_ONBOARDING_HELD,
        OperatorTargetType.SELLER_ONBOARDING,
        onboarding.getId(),
        command.reason()));
    return OperatorOnboardingResult.from(operatorPersistencePort.saveOnboarding(onboarding));
  }

  @Override
  @Transactional(readOnly = true)
  public PageResult<OperatorOrderResult> getOrders(OperatorOrderQuery query) {
    return PageResult.from(operatorPersistencePort
        .findOrders(
            query.storeId(),
            query.buyerUserId(),
            query.status(),
            toNullableInstant(query.startDate()),
            toExclusiveInstant(query.endDate()),
            query.pageQuery().toPageable())
        .map(OperatorOrderResult::from));
  }

  @Override
  @Transactional(readOnly = true)
  public OperatorOrderResult getOrder(UUID orderId) {
    return OperatorOrderResult.from(findOrder(orderId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderStatusHistoryResult> getOrderHistories(UUID orderId) {
    findOrder(orderId);
    return orderStatusHistoryPersistencePort.findAllByOrderId(orderId).stream()
        .map(OrderStatusHistoryResult::from)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public String exportOrdersCsv(OperatorOrderQuery query) {
    List<OperatorOrderResult> orders = operatorPersistencePort
        .findOrders(
            query.storeId(),
            query.buyerUserId(),
            query.status(),
            toNullableInstant(query.startDate()),
            toExclusiveInstant(query.endDate()),
            EXPORT_PAGEABLE)
        .map(OperatorOrderResult::from)
        .getContent();
    return toCsv(
        List.of(
            "id",
            "storeId",
            "buyerUserId",
            "orderNumber",
            "paidAmount",
            "pickupAt",
            "status",
            "cancelRequestedAt",
            "createdAt"),
        orders.stream()
            .map(order -> List.of(
                value(order.id()),
                value(order.storeId()),
                value(order.buyerUserId()),
                value(order.orderNumber()),
                value(order.paidAmount()),
                value(order.pickupAt()),
                value(order.status()),
                value(order.cancelRequestedAt()),
                value(order.createdAt())))
            .toList());
  }

  @Override
  @Transactional(readOnly = true)
  public PageResult<OperatorPaymentAttemptResult> getPayments(OperatorPaymentQuery query) {
    return PageResult.from(operatorPersistencePort
        .findPayments(
            query.status(),
            toNullableInstant(query.startDate()),
            toExclusiveInstant(query.endDate()),
            query.pageQuery().toPageable())
        .map(OperatorPaymentAttemptResult::from));
  }

  @Override
  @Transactional(readOnly = true)
  public OperatorPaymentAttemptResult getPayment(UUID paymentAttemptId) {
    return OperatorPaymentAttemptResult.from(operatorPersistencePort
        .findPayment(paymentAttemptId)
        .orElseThrow(() -> new BaseException(CommonErrorCode.INVALID_ID)));
  }

  @Override
  @Transactional(readOnly = true)
  public String exportPaymentsCsv(OperatorPaymentQuery query) {
    List<OperatorPaymentAttemptResult> payments = operatorPersistencePort
        .findPayments(
            query.status(),
            toNullableInstant(query.startDate()),
            toExclusiveInstant(query.endDate()),
            EXPORT_PAGEABLE)
        .map(OperatorPaymentAttemptResult::from)
        .getContent();
    return toCsv(
        List.of(
            "id",
            "confirmationId",
            "payerUserId",
            "point3SessionId",
            "amount",
            "status",
            "failureCode",
            "createdAt",
            "completedAt"),
        payments.stream()
            .map(payment -> List.of(
                value(payment.id()),
                value(payment.confirmationId()),
                value(payment.payerUserId()),
                value(payment.point3SessionId()),
                value(payment.amount()),
                value(payment.status()),
                value(payment.failureCode()),
                value(payment.createdAt()),
                value(payment.completedAt())))
            .toList());
  }

  @Override
  @Transactional(readOnly = true)
  public PageResult<OperatorRefundResult> getRefunds(OperatorRefundQuery query) {
    return PageResult.from(operatorPersistencePort
        .findRefunds(
            query.status(),
            toNullableInstant(query.startDate()),
            toExclusiveInstant(query.endDate()),
            query.pageQuery().toPageable())
        .map(OperatorRefundResult::from));
  }

  @Override
  @Transactional(readOnly = true)
  public OperatorRefundResult getRefund(UUID refundId) {
    return OperatorRefundResult.from(operatorPersistencePort
        .findRefund(refundId)
        .orElseThrow(() -> new BaseException(CommonErrorCode.INVALID_ID)));
  }

  @Override
  @Transactional(readOnly = true)
  public String exportRefundsCsv(OperatorRefundQuery query) {
    List<OperatorRefundResult> refunds = operatorPersistencePort
        .findRefunds(
            query.status(),
            toNullableInstant(query.startDate()),
            toExclusiveInstant(query.endDate()),
            EXPORT_PAGEABLE)
        .map(OperatorRefundResult::from)
        .getContent();
    return toCsv(
        List.of(
            "id",
            "orderId",
            "paymentAttemptId",
            "requestedBy",
            "amount",
            "reason",
            "status",
            "createdAt",
            "completedAt"),
        refunds.stream()
            .map(refund -> List.of(
                value(refund.id()),
                value(refund.orderId()),
                value(refund.paymentAttemptId()),
                value(refund.requestedBy()),
                value(refund.amount()),
                value(refund.reason()),
                value(refund.status()),
                value(refund.createdAt()),
                value(refund.completedAt())))
            .toList());
  }

  @Override
  public OperatorReportResult createReport(CreateReportCommand command) {
    Report report = Report.create(
        command.reporterUserId(),
        command.targetType(),
        command.targetId(),
        command.reason(),
        command.evidence());
    return OperatorReportResult.from(operatorPersistencePort.saveReport(report));
  }

  @Override
  @Transactional(readOnly = true)
  public PageResult<OperatorReportResult> getReports(OperatorReportQuery query) {
    return PageResult.from(operatorPersistencePort
        .findReports(
            query.status(),
            query.targetType(),
            query.keyword(),
            query.pageQuery().toPageable())
        .map(OperatorReportResult::from));
  }

  @Override
  @Transactional(readOnly = true)
  public OperatorReportResult getReport(UUID reportId) {
    return OperatorReportResult.from(findReport(reportId));
  }

  @Override
  public OperatorReportResult reviewReport(ReviewReportCommand command) {
    Report report = findReport(command.reportId());
    report.review(command.operatorUserId());
    operatorPersistencePort.saveActionLog(OperatorActionLog.create(
        command.operatorUserId(),
        OperatorActionType.REPORT_REVIEWED,
        OperatorTargetType.REPORT,
        report.getId(),
        "REPORT_REVIEWED"));
    return OperatorReportResult.from(operatorPersistencePort.saveReport(report));
  }

  @Override
  public OperatorReportResult resolveReport(ResolveReportCommand command) {
    Report report = findReport(command.reportId());
    report.resolve(
        command.operatorUserId(), command.status(), command.resolution(), Instant.now(clock));
    applyReportTargetAction(
        report, command.targetAction(), command.operatorUserId(), command.resolution());
    operatorPersistencePort.saveActionLog(OperatorActionLog.create(
        command.operatorUserId(),
        OperatorActionType.REPORT_RESOLVED,
        OperatorTargetType.REPORT,
        report.getId(),
        command.resolution()));
    return OperatorReportResult.from(operatorPersistencePort.saveReport(report));
  }

  private void applyReportTargetAction(
      Report report, ReportTargetAction targetAction, UUID operatorUserId, String reason) {
    ReportTargetAction resolvedAction =
        targetAction == null ? ReportTargetAction.NONE : targetAction;
    if (resolvedAction == ReportTargetAction.NONE) {
      return;
    }

    if (report.getTargetType() == ReportTargetType.USER) {
      applyUserReportAction(report.getTargetId(), resolvedAction, operatorUserId, reason);
      return;
    }
    if (report.getTargetType() == ReportTargetType.STORE) {
      applyStoreReportAction(report.getTargetId(), resolvedAction, operatorUserId, reason);
      return;
    }
    if (report.getTargetType() == ReportTargetType.GALLERY_ITEM) {
      applyGalleryReportAction(report.getTargetId(), resolvedAction, operatorUserId, reason);
      return;
    }

    throw new BaseException(CommonErrorCode.INVALID_INPUT, "Report target action is not supported");
  }

  private void applyUserReportAction(
      UUID targetId, ReportTargetAction action, UUID operatorUserId, String reason) {
    if (action != ReportTargetAction.BAN && action != ReportTargetAction.UNBAN) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT, "Invalid user report action");
    }
    if (targetId.equals(operatorUserId)) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT, "Operator can not change own status");
    }
    User user = findUser(targetId);
    changeUserStatus(
        user, action == ReportTargetAction.BAN ? UserStatus.BANNED : UserStatus.ACTIVE);
    operatorPersistencePort.saveUser(user);
    operatorPersistencePort.saveActionLog(OperatorActionLog.create(
        operatorUserId,
        OperatorActionType.USER_STATUS_CHANGED,
        OperatorTargetType.USER,
        targetId,
        reason));
  }

  private void applyStoreReportAction(
      UUID targetId, ReportTargetAction action, UUID operatorUserId, String reason) {
    StoreStatus status =
        switch (action) {
          case HIDE -> StoreStatus.INACTIVE;
          case RESTORE, UNSUSPEND -> StoreStatus.ACTIVE;
          case SUSPEND -> StoreStatus.SUSPENDED;
          default ->
            throw new BaseException(CommonErrorCode.INVALID_INPUT, "Invalid store report action");
        };
    Store store = findStore(targetId);
    changeStoreStatus(store, status);
    operatorPersistencePort.saveStore(store);
    operatorPersistencePort.saveActionLog(OperatorActionLog.create(
        operatorUserId,
        OperatorActionType.STORE_STATUS_CHANGED,
        OperatorTargetType.STORE,
        targetId,
        reason));
  }

  private void applyGalleryReportAction(
      UUID targetId, ReportTargetAction action, UUID operatorUserId, String reason) {
    if (action != ReportTargetAction.HIDE && action != ReportTargetAction.RESTORE) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT, "Invalid gallery report action");
    }
    StoreGalleryItem item = findGalleryItem(targetId);
    if (action == ReportTargetAction.HIDE) {
      item.hide();
    } else {
      item.show();
    }
    operatorPersistencePort.saveGalleryItem(item);
    operatorPersistencePort.saveActionLog(OperatorActionLog.create(
        operatorUserId,
        OperatorActionType.GALLERY_ITEM_STATUS_CHANGED,
        OperatorTargetType.GALLERY_ITEM,
        targetId,
        reason));
  }

  @Override
  public OperatorServiceInquiryResult createServiceInquiry(CreateServiceInquiryCommand command) {
    ServiceInquiry inquiry =
        ServiceInquiry.create(command.requesterUserId(), command.title(), command.body());
    return OperatorServiceInquiryResult.from(operatorPersistencePort.saveServiceInquiry(inquiry));
  }

  @Override
  @Transactional(readOnly = true)
  public PageResult<OperatorServiceInquiryResult> getServiceInquiries(
      OperatorServiceInquiryQuery query) {
    return PageResult.from(operatorPersistencePort
        .findServiceInquiries(
            query.status(),
            query.assigneeOperatorId(),
            query.keyword(),
            query.pageQuery().toPageable())
        .map(OperatorServiceInquiryResult::from));
  }

  @Override
  @Transactional(readOnly = true)
  public OperatorServiceInquiryResult getServiceInquiry(UUID serviceInquiryId) {
    return OperatorServiceInquiryResult.from(findServiceInquiry(serviceInquiryId));
  }

  @Override
  public OperatorServiceInquiryResult assignServiceInquiry(AssignServiceInquiryCommand command) {
    ServiceInquiry inquiry = findServiceInquiry(command.serviceInquiryId());
    inquiry.assign(command.operatorUserId());
    operatorPersistencePort.saveActionLog(OperatorActionLog.create(
        command.operatorUserId(),
        OperatorActionType.SERVICE_INQUIRY_ASSIGNED,
        OperatorTargetType.SERVICE_INQUIRY,
        inquiry.getId(),
        "SERVICE_INQUIRY_ASSIGNED"));
    return OperatorServiceInquiryResult.from(operatorPersistencePort.saveServiceInquiry(inquiry));
  }

  @Override
  public OperatorServiceInquiryResult answerServiceInquiry(AnswerServiceInquiryCommand command) {
    ServiceInquiry inquiry = findServiceInquiry(command.serviceInquiryId());
    inquiry.answer(command.operatorUserId(), command.answer(), Instant.now(clock));
    operatorPersistencePort.saveActionLog(OperatorActionLog.create(
        command.operatorUserId(),
        OperatorActionType.SERVICE_INQUIRY_ANSWERED,
        OperatorTargetType.SERVICE_INQUIRY,
        inquiry.getId(),
        command.answer()));
    return OperatorServiceInquiryResult.from(operatorPersistencePort.saveServiceInquiry(inquiry));
  }

  @Override
  public OperatorServiceInquiryResult closeServiceInquiry(CloseServiceInquiryCommand command) {
    ServiceInquiry inquiry = findServiceInquiry(command.serviceInquiryId());
    inquiry.close(command.operatorUserId(), Instant.now(clock));
    operatorPersistencePort.saveActionLog(OperatorActionLog.create(
        command.operatorUserId(),
        OperatorActionType.SERVICE_INQUIRY_CLOSED,
        OperatorTargetType.SERVICE_INQUIRY,
        inquiry.getId(),
        "SERVICE_INQUIRY_CLOSED"));
    return OperatorServiceInquiryResult.from(operatorPersistencePort.saveServiceInquiry(inquiry));
  }

  @Override
  @Transactional(readOnly = true)
  public PageResult<OperatorActionLogResult> getActionLogs(OperatorActionLogQuery query) {
    return PageResult.from(operatorPersistencePort
        .findActionLogs(
            query.operatorUserId(),
            query.actionType(),
            query.targetType(),
            query.targetId(),
            toNullableInstant(query.startDate()),
            toExclusiveInstant(query.endDate()),
            query.pageQuery().toPageable())
        .map(OperatorActionLogResult::from));
  }

  private String toCsv(List<String> headers, List<List<String>> rows) {
    StringBuilder builder = new StringBuilder();
    builder.append(String.join(",", headers)).append('\n');
    for (List<String> row : rows) {
      builder
          .append(String.join(",", row.stream().map(this::escapeCsv).toList()))
          .append('\n');
    }
    return builder.toString();
  }

  private String escapeCsv(String value) {
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

  private String value(Object value) {
    return value == null ? "" : value.toString();
  }

  private void changeUserStatus(User user, UserStatus status) {
    try {
      if (user.getStatus() == status) {
        return;
      }
      if (status == UserStatus.ACTIVE) {
        user.unban();
      } else if (status == UserStatus.BANNED) {
        user.ban();
      } else {
        user.withdraw();
      }
    } catch (IllegalArgumentException e) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT, e.getMessage());
    }
  }

  private void changeStoreStatus(Store store, StoreStatus status) {
    try {
      if (store.getStatus() == status) {
        return;
      }
      if (status == StoreStatus.ACTIVE) {
        store.active();
      } else if (status == StoreStatus.INACTIVE) {
        store.inactive();
      } else if (status == StoreStatus.SUSPENDED) {
        store.suspend();
      } else {
        store.delete();
      }
    } catch (IllegalArgumentException e) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT, e.getMessage());
    }
  }

  private User findUser(UUID userId) {
    return operatorPersistencePort
        .findUser(userId)
        .orElseThrow(() -> new BaseException(CommonErrorCode.INVALID_ID));
  }

  private Store findStore(UUID storeId) {
    return operatorPersistencePort
        .findStore(storeId)
        .orElseThrow(() -> new BaseException(CommonErrorCode.INVALID_ID));
  }

  private StoreGalleryItem findGalleryItem(UUID galleryItemId) {
    return operatorPersistencePort
        .findGalleryItem(galleryItemId)
        .orElseThrow(() -> new BaseException(CommonErrorCode.INVALID_ID));
  }

  private SellerOnboarding findOnboarding(UUID onboardingId) {
    return operatorPersistencePort
        .findOnboarding(onboardingId)
        .orElseThrow(() -> new BaseException(CommonErrorCode.INVALID_ID));
  }

  private Order findOrder(UUID orderId) {
    return operatorPersistencePort
        .findOrder(orderId)
        .orElseThrow(() -> new BaseException(CommonErrorCode.INVALID_ID));
  }

  private Report findReport(UUID reportId) {
    return operatorPersistencePort
        .findReport(reportId)
        .orElseThrow(() -> new BaseException(CommonErrorCode.INVALID_ID));
  }

  private ServiceInquiry findServiceInquiry(UUID serviceInquiryId) {
    return operatorPersistencePort
        .findServiceInquiry(serviceInquiryId)
        .orElseThrow(() -> new BaseException(CommonErrorCode.INVALID_ID));
  }

  private void validateReason(String reason) {
    if (reason == null || reason.isBlank()) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT, "reason must not be blank");
    }
  }

  private Instant toNullableInstant(LocalDate date) {
    return date == null ? null : toInstant(date);
  }

  private Instant toExclusiveInstant(LocalDate date) {
    return date == null ? null : toInstant(date.plusDays(1));
  }

  private Instant toInstant(LocalDate date) {
    return date.atStartOfDay(KOREA_ZONE).toInstant();
  }
}
