package io.point3.p3api.operator.controller;

import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import io.point3.p3api.operator.application.OperatorManagementUseCase;
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
import io.point3.p3api.operator.application.query.OperatorActionLogQuery;
import io.point3.p3api.operator.application.query.OperatorDashboardQuery;
import io.point3.p3api.operator.application.query.OperatorGalleryItemQuery;
import io.point3.p3api.operator.application.query.OperatorOnboardingQuery;
import io.point3.p3api.operator.application.query.OperatorOrderQuery;
import io.point3.p3api.operator.application.query.OperatorPageQuery;
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
import io.point3.p3api.operator.controller.request.OperatorReasonRequest;
import io.point3.p3api.operator.controller.request.OperatorStatusChangeRequest;
import io.point3.p3api.operator.controller.request.ReportCreateRequest;
import io.point3.p3api.operator.controller.request.ReportResolveRequest;
import io.point3.p3api.operator.controller.request.ServiceInquiryAnswerRequest;
import io.point3.p3api.operator.controller.request.ServiceInquiryCreateRequest;
import io.point3.p3api.operator.domain.type.OperatorActionType;
import io.point3.p3api.operator.domain.type.OperatorTargetType;
import io.point3.p3api.operator.domain.type.ReportStatus;
import io.point3.p3api.operator.domain.type.ReportTargetType;
import io.point3.p3api.operator.domain.type.ServiceInquiryStatus;
import io.point3.p3api.order.domain.type.OrderStatus;
import io.point3.p3api.payment.domain.type.PaymentAttemptStatus;
import io.point3.p3api.payment.domain.type.RefundStatus;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import io.point3.p3api.store.domain.type.StoreStatus;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.domain.type.UserStatus;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OperatorManagementController {

  private final OperatorManagementUseCase operatorManagementUseCase;

  @GetMapping("/operator/dashboard")
  public ApiResponse<OperatorDashboardResult> getDashboard(
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(
        operatorManagementUseCase.getDashboard(new OperatorDashboardQuery(startDate, endDate)));
  }

  @GetMapping("/operator/users")
  public ApiResponse<PageResult<OperatorUserResult>> getUsers(
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) UserRole role,
      @RequestParam(required = false) UserStatus status,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getUsers(
        new OperatorUserQuery(keyword, role, status, new OperatorPageQuery(page, size))));
  }

  @GetMapping("/operator/users/{userId}")
  public ApiResponse<OperatorUserResult> getUser(
      @Authenticated CurrentUser currentUser, @PathVariable UUID userId) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getUser(userId));
  }

  @PatchMapping("/operator/users/{userId}/status")
  public ApiResponse<OperatorUserResult> changeUserStatus(
      @Authenticated CurrentUser currentUser,
      @PathVariable UUID userId,
      @Valid @RequestBody OperatorStatusChangeRequest<UserStatus> request) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.changeUserStatus(ChangeUserStatusCommand.of(
        userId, currentUser.userId(), request.status(), request.reason())));
  }

  @GetMapping("/operator/stores")
  public ApiResponse<PageResult<OperatorStoreResult>> getStores(
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) StoreStatus status,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getStores(
        new OperatorStoreQuery(keyword, status, new OperatorPageQuery(page, size))));
  }

  @GetMapping("/operator/stores/{storeId}")
  public ApiResponse<OperatorStoreResult> getStore(
      @Authenticated CurrentUser currentUser, @PathVariable UUID storeId) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getStore(storeId));
  }

  @PatchMapping("/operator/stores/{storeId}/status")
  public ApiResponse<OperatorStoreResult> changeStoreStatus(
      @Authenticated CurrentUser currentUser,
      @PathVariable UUID storeId,
      @Valid @RequestBody OperatorStatusChangeRequest<StoreStatus> request) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.changeStoreStatus(ChangeStoreStatusCommand.of(
        storeId, currentUser.userId(), request.status(), request.reason())));
  }

  @GetMapping("/operator/gallery-items")
  public ApiResponse<PageResult<OperatorGalleryItemResult>> getGalleryItems(
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) UUID storeId,
      @RequestParam(required = false) UUID assetId,
      @RequestParam(required = false) StoreGalleryItemStatus status,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getGalleryItems(
        new OperatorGalleryItemQuery(storeId, assetId, status, new OperatorPageQuery(page, size))));
  }

  @GetMapping("/operator/gallery-items/{galleryItemId}")
  public ApiResponse<OperatorGalleryItemResult> getGalleryItem(
      @Authenticated CurrentUser currentUser, @PathVariable UUID galleryItemId) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getGalleryItem(galleryItemId));
  }

  @PatchMapping("/operator/gallery-items/{galleryItemId}/status")
  public ApiResponse<OperatorGalleryItemResult> changeGalleryItemStatus(
      @Authenticated CurrentUser currentUser,
      @PathVariable UUID galleryItemId,
      @Valid @RequestBody OperatorStatusChangeRequest<StoreGalleryItemStatus> request) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(
        operatorManagementUseCase.changeGalleryItemStatus(ChangeGalleryItemStatusCommand.of(
            galleryItemId, currentUser.userId(), request.status(), request.reason())));
  }

  @GetMapping("/operator/onboardings")
  public ApiResponse<PageResult<OperatorOnboardingResult>> getOnboardings(
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) SellerOnboardingStatus status,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getOnboardings(
        new OperatorOnboardingQuery(status, new OperatorPageQuery(page, size))));
  }

  @GetMapping("/operator/onboardings/{onboardingId}")
  public ApiResponse<OperatorOnboardingResult> getOnboarding(
      @Authenticated CurrentUser currentUser, @PathVariable UUID onboardingId) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getOnboarding(onboardingId));
  }

  @PatchMapping("/operator/onboardings/{onboardingId}/hold")
  public ApiResponse<OperatorOnboardingResult> holdOnboarding(
      @Authenticated CurrentUser currentUser,
      @PathVariable UUID onboardingId,
      @Valid @RequestBody OperatorReasonRequest request) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.holdOnboarding(
        HoldSellerOnboardingCommand.of(onboardingId, currentUser.userId(), request.reason())));
  }

  @GetMapping("/operator/orders")
  public ApiResponse<PageResult<OperatorOrderResult>> getOrders(
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) UUID storeId,
      @RequestParam(required = false) UUID buyerUserId,
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getOrders(new OperatorOrderQuery(
        storeId, buyerUserId, status, startDate, endDate, new OperatorPageQuery(page, size))));
  }

  @GetMapping("/operator/orders/{orderId}")
  public ApiResponse<OperatorOrderResult> getOrder(
      @Authenticated CurrentUser currentUser, @PathVariable UUID orderId) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getOrder(orderId));
  }

  @GetMapping("/operator/orders/{orderId}/histories")
  public ApiResponse<List<OrderStatusHistoryResult>> getOrderHistories(
      @Authenticated CurrentUser currentUser, @PathVariable UUID orderId) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getOrderHistories(orderId));
  }

  @GetMapping("/operator/orders/export")
  public ResponseEntity<String> exportOrders(
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) UUID storeId,
      @RequestParam(required = false) UUID buyerUserId,
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate) {
    RoleGuard.requireOperator(currentUser);
    String csv = operatorManagementUseCase.exportOrdersCsv(
        new OperatorOrderQuery(storeId, buyerUserId, status, startDate, endDate, null));
    return csvResponse("operator-orders.csv", csv);
  }

  @GetMapping("/operator/payments")
  public ApiResponse<PageResult<OperatorPaymentAttemptResult>> getPayments(
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) PaymentAttemptStatus status,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getPayments(
        new OperatorPaymentQuery(status, startDate, endDate, new OperatorPageQuery(page, size))));
  }

  @GetMapping("/operator/payments/needs-confirmation")
  public ApiResponse<PageResult<OperatorPaymentAttemptResult>> getNeedsConfirmationPayments(
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getPayments(new OperatorPaymentQuery(
        PaymentAttemptStatus.NEEDS_CONFIRMATION, null, null, new OperatorPageQuery(page, size))));
  }

  @GetMapping("/operator/payments/{paymentAttemptId}")
  public ApiResponse<OperatorPaymentAttemptResult> getPayment(
      @Authenticated CurrentUser currentUser, @PathVariable UUID paymentAttemptId) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getPayment(paymentAttemptId));
  }

  @GetMapping("/operator/payments/export")
  public ResponseEntity<String> exportPayments(
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) PaymentAttemptStatus status,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate) {
    RoleGuard.requireOperator(currentUser);
    String csv = operatorManagementUseCase.exportPaymentsCsv(
        new OperatorPaymentQuery(status, startDate, endDate, null));
    return csvResponse("operator-payments.csv", csv);
  }

  @GetMapping("/operator/refunds")
  public ApiResponse<PageResult<OperatorRefundResult>> getRefunds(
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) RefundStatus status,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getRefunds(
        new OperatorRefundQuery(status, startDate, endDate, new OperatorPageQuery(page, size))));
  }

  @GetMapping("/operator/refunds/{refundId}")
  public ApiResponse<OperatorRefundResult> getRefund(
      @Authenticated CurrentUser currentUser, @PathVariable UUID refundId) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getRefund(refundId));
  }

  @GetMapping("/operator/refunds/export")
  public ResponseEntity<String> exportRefunds(
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) RefundStatus status,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate) {
    RoleGuard.requireOperator(currentUser);
    String csv = operatorManagementUseCase.exportRefundsCsv(
        new OperatorRefundQuery(status, startDate, endDate, null));
    return csvResponse("operator-refunds.csv", csv);
  }

  @PostMapping("/reports")
  public ApiResponse<OperatorReportResult> createReport(
      @Authenticated CurrentUser currentUser, @Valid @RequestBody ReportCreateRequest request) {
    return ApiResponse.ok(operatorManagementUseCase.createReport(CreateReportCommand.of(
        currentUser.userId(),
        request.targetType(),
        request.targetId(),
        request.reason(),
        request.evidence())));
  }

  @GetMapping("/operator/reports")
  public ApiResponse<PageResult<OperatorReportResult>> getReports(
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) ReportStatus status,
      @RequestParam(required = false) ReportTargetType targetType,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getReports(
        new OperatorReportQuery(status, targetType, keyword, new OperatorPageQuery(page, size))));
  }

  @GetMapping("/operator/reports/{reportId}")
  public ApiResponse<OperatorReportResult> getReport(
      @Authenticated CurrentUser currentUser, @PathVariable UUID reportId) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getReport(reportId));
  }

  @PatchMapping("/operator/reports/{reportId}/review")
  public ApiResponse<OperatorReportResult> reviewReport(
      @Authenticated CurrentUser currentUser, @PathVariable UUID reportId) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.reviewReport(
        ReviewReportCommand.of(reportId, currentUser.userId())));
  }

  @PatchMapping("/operator/reports/{reportId}/resolve")
  public ApiResponse<OperatorReportResult> resolveReport(
      @Authenticated CurrentUser currentUser,
      @PathVariable UUID reportId,
      @Valid @RequestBody ReportResolveRequest request) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.resolveReport(ResolveReportCommand.of(
        reportId,
        currentUser.userId(),
        request.status(),
        request.resolution(),
        request.targetAction())));
  }

  @PostMapping("/service-inquiries")
  public ApiResponse<OperatorServiceInquiryResult> createServiceInquiry(
      @Authenticated CurrentUser currentUser,
      @Valid @RequestBody ServiceInquiryCreateRequest request) {
    return ApiResponse.ok(operatorManagementUseCase.createServiceInquiry(
        CreateServiceInquiryCommand.of(currentUser.userId(), request.title(), request.body())));
  }

  @GetMapping("/operator/service-inquiries")
  public ApiResponse<PageResult<OperatorServiceInquiryResult>> getServiceInquiries(
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) ServiceInquiryStatus status,
      @RequestParam(required = false) UUID assigneeOperatorId,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(
        operatorManagementUseCase.getServiceInquiries(new OperatorServiceInquiryQuery(
            status, assigneeOperatorId, keyword, new OperatorPageQuery(page, size))));
  }

  @GetMapping("/operator/service-inquiries/{serviceInquiryId}")
  public ApiResponse<OperatorServiceInquiryResult> getServiceInquiry(
      @Authenticated CurrentUser currentUser, @PathVariable UUID serviceInquiryId) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getServiceInquiry(serviceInquiryId));
  }

  @PatchMapping("/operator/service-inquiries/{serviceInquiryId}/assign")
  public ApiResponse<OperatorServiceInquiryResult> assignServiceInquiry(
      @Authenticated CurrentUser currentUser, @PathVariable UUID serviceInquiryId) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.assignServiceInquiry(
        AssignServiceInquiryCommand.of(serviceInquiryId, currentUser.userId())));
  }

  @PatchMapping("/operator/service-inquiries/{serviceInquiryId}/answer")
  public ApiResponse<OperatorServiceInquiryResult> answerServiceInquiry(
      @Authenticated CurrentUser currentUser,
      @PathVariable UUID serviceInquiryId,
      @Valid @RequestBody ServiceInquiryAnswerRequest request) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.answerServiceInquiry(
        AnswerServiceInquiryCommand.of(serviceInquiryId, currentUser.userId(), request.answer())));
  }

  @PatchMapping("/operator/service-inquiries/{serviceInquiryId}/close")
  public ApiResponse<OperatorServiceInquiryResult> closeServiceInquiry(
      @Authenticated CurrentUser currentUser, @PathVariable UUID serviceInquiryId) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.closeServiceInquiry(
        CloseServiceInquiryCommand.of(serviceInquiryId, currentUser.userId())));
  }

  @GetMapping("/operator/action-logs")
  public ApiResponse<PageResult<OperatorActionLogResult>> getActionLogs(
      @Authenticated CurrentUser currentUser,
      @RequestParam(required = false) UUID operatorUserId,
      @RequestParam(required = false) OperatorActionType actionType,
      @RequestParam(required = false) OperatorTargetType targetType,
      @RequestParam(required = false) UUID targetId,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    RoleGuard.requireOperator(currentUser);
    return ApiResponse.ok(operatorManagementUseCase.getActionLogs(new OperatorActionLogQuery(
        operatorUserId,
        actionType,
        targetType,
        targetId,
        startDate,
        endDate,
        new OperatorPageQuery(page, size))));
  }

  private ResponseEntity<String> csvResponse(String fileName, String csv) {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
        .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
        .body(csv);
  }
}
