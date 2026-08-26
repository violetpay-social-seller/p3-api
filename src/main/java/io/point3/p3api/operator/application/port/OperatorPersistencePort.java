package io.point3.p3api.operator.application.port;

import io.point3.p3api.gallery.domain.entity.StoreGalleryItem;
import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import io.point3.p3api.operator.application.result.StatusCountResult;
import io.point3.p3api.operator.domain.entity.OperatorActionLog;
import io.point3.p3api.operator.domain.entity.Report;
import io.point3.p3api.operator.domain.entity.ServiceInquiry;
import io.point3.p3api.operator.domain.type.OperatorActionType;
import io.point3.p3api.operator.domain.type.OperatorTargetType;
import io.point3.p3api.operator.domain.type.ReportStatus;
import io.point3.p3api.operator.domain.type.ReportTargetType;
import io.point3.p3api.operator.domain.type.ServiceInquiryStatus;
import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.type.OrderStatus;
import io.point3.p3api.payment.domain.entity.PaymentAttempt;
import io.point3.p3api.payment.domain.entity.Refund;
import io.point3.p3api.payment.domain.type.PaymentAttemptStatus;
import io.point3.p3api.payment.domain.type.RefundStatus;
import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.store.domain.type.StoreStatus;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.domain.type.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OperatorPersistencePort {

  long countUsers();

  long countUsersByRole(UserRole role);

  long countStores();

  long countPendingOnboardings();

  long countOpenReports();

  long countOpenServiceInquiries();

  long sumSucceededPaymentAmount(Instant startInclusive, Instant endExclusive);

  long sumCompletedRefundAmount(Instant startInclusive, Instant endExclusive);

  List<StatusCountResult> countOrdersByStatus();

  List<StatusCountResult> countPaymentsByStatus();

  List<StatusCountResult> countRefundsByStatus();

  Page<User> findUsers(String keyword, UserRole role, UserStatus status, Pageable pageable);

  Optional<User> findUser(UUID userId);

  User saveUser(User user);

  Page<Store> findStores(String keyword, StoreStatus status, Pageable pageable);

  Optional<Store> findStore(UUID storeId);

  Store saveStore(Store store);

  Page<StoreGalleryItem> findGalleryItems(
      UUID storeId, UUID assetId, StoreGalleryItemStatus status, Pageable pageable);

  Optional<StoreGalleryItem> findGalleryItem(UUID galleryItemId);

  StoreGalleryItem saveGalleryItem(StoreGalleryItem item);

  Page<Order> findOrders(
      UUID storeId,
      UUID buyerUserId,
      OrderStatus status,
      Instant startInclusive,
      Instant endExclusive,
      Pageable pageable);

  Optional<Order> findOrder(UUID orderId);

  Page<PaymentAttempt> findPayments(
      PaymentAttemptStatus status, Instant startInclusive, Instant endExclusive, Pageable pageable);

  Optional<PaymentAttempt> findPayment(UUID paymentAttemptId);

  Page<Refund> findRefunds(
      RefundStatus status, Instant startInclusive, Instant endExclusive, Pageable pageable);

  Optional<Refund> findRefund(UUID refundId);

  Page<SellerOnboarding> findOnboardings(SellerOnboardingStatus status, Pageable pageable);

  Optional<SellerOnboarding> findOnboarding(UUID onboardingId);

  SellerOnboarding saveOnboarding(SellerOnboarding onboarding);

  Page<Report> findReports(
      ReportStatus status, ReportTargetType targetType, String keyword, Pageable pageable);

  Optional<Report> findReport(UUID reportId);

  Report saveReport(Report report);

  Page<ServiceInquiry> findServiceInquiries(
      ServiceInquiryStatus status, UUID assigneeOperatorId, String keyword, Pageable pageable);

  Optional<ServiceInquiry> findServiceInquiry(UUID serviceInquiryId);

  ServiceInquiry saveServiceInquiry(ServiceInquiry inquiry);

  OperatorActionLog saveActionLog(OperatorActionLog actionLog);

  Page<OperatorActionLog> findActionLogs(
      UUID operatorUserId,
      OperatorActionType actionType,
      OperatorTargetType targetType,
      UUID targetId,
      Instant startInclusive,
      Instant endExclusive,
      Pageable pageable);
}
