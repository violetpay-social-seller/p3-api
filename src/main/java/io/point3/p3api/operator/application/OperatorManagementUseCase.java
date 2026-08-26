package io.point3.p3api.operator.application;

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
import java.util.List;
import java.util.UUID;

public interface OperatorManagementUseCase {

  OperatorDashboardResult getDashboard(OperatorDashboardQuery query);

  PageResult<OperatorUserResult> getUsers(OperatorUserQuery query);

  OperatorUserResult getUser(UUID userId);

  OperatorUserResult changeUserStatus(ChangeUserStatusCommand command);

  PageResult<OperatorStoreResult> getStores(OperatorStoreQuery query);

  OperatorStoreResult getStore(UUID storeId);

  OperatorStoreResult changeStoreStatus(ChangeStoreStatusCommand command);

  PageResult<OperatorGalleryItemResult> getGalleryItems(OperatorGalleryItemQuery query);

  OperatorGalleryItemResult getGalleryItem(UUID galleryItemId);

  OperatorGalleryItemResult changeGalleryItemStatus(ChangeGalleryItemStatusCommand command);

  PageResult<OperatorOnboardingResult> getOnboardings(OperatorOnboardingQuery query);

  OperatorOnboardingResult getOnboarding(UUID onboardingId);

  OperatorOnboardingResult holdOnboarding(HoldSellerOnboardingCommand command);

  PageResult<OperatorOrderResult> getOrders(OperatorOrderQuery query);

  OperatorOrderResult getOrder(UUID orderId);

  List<OrderStatusHistoryResult> getOrderHistories(UUID orderId);

  String exportOrdersCsv(OperatorOrderQuery query);

  PageResult<OperatorPaymentAttemptResult> getPayments(OperatorPaymentQuery query);

  OperatorPaymentAttemptResult getPayment(UUID paymentAttemptId);

  String exportPaymentsCsv(OperatorPaymentQuery query);

  PageResult<OperatorRefundResult> getRefunds(OperatorRefundQuery query);

  OperatorRefundResult getRefund(UUID refundId);

  String exportRefundsCsv(OperatorRefundQuery query);

  OperatorReportResult createReport(CreateReportCommand command);

  PageResult<OperatorReportResult> getReports(OperatorReportQuery query);

  OperatorReportResult getReport(UUID reportId);

  OperatorReportResult reviewReport(ReviewReportCommand command);

  OperatorReportResult resolveReport(ResolveReportCommand command);

  OperatorServiceInquiryResult createServiceInquiry(CreateServiceInquiryCommand command);

  PageResult<OperatorServiceInquiryResult> getServiceInquiries(OperatorServiceInquiryQuery query);

  OperatorServiceInquiryResult getServiceInquiry(UUID serviceInquiryId);

  OperatorServiceInquiryResult assignServiceInquiry(AssignServiceInquiryCommand command);

  OperatorServiceInquiryResult answerServiceInquiry(AnswerServiceInquiryCommand command);

  OperatorServiceInquiryResult closeServiceInquiry(CloseServiceInquiryCommand command);

  PageResult<OperatorActionLogResult> getActionLogs(OperatorActionLogQuery query);
}
