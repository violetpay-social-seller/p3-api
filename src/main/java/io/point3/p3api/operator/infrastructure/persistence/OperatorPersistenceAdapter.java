package io.point3.p3api.operator.infrastructure.persistence;

import io.point3.p3api.gallery.domain.entity.StoreGalleryItem;
import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import io.point3.p3api.gallery.infrastructure.persistence.GalleryItemJpaRepository;
import io.point3.p3api.operator.application.port.OperatorPersistencePort;
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
import io.point3.p3api.order.infrastructure.persistence.OrderJpaRepository;
import io.point3.p3api.payment.domain.entity.PaymentAttempt;
import io.point3.p3api.payment.domain.entity.Refund;
import io.point3.p3api.payment.domain.type.PaymentAttemptStatus;
import io.point3.p3api.payment.domain.type.RefundStatus;
import io.point3.p3api.payment.infrastructure.persistence.PaymentAttemptJpaRepository;
import io.point3.p3api.payment.infrastructure.persistence.RefundJpaRepository;
import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import io.point3.p3api.seller.infrastructure.persistence.SellerOnboardingJpaRepository;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.store.domain.type.StoreStatus;
import io.point3.p3api.store.infrastructure.persistence.StoreJpaRepository;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.domain.type.UserStatus;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class OperatorPersistenceAdapter implements OperatorPersistencePort {

  private final UserJpaRepository userJpaRepository;
  private final StoreJpaRepository storeJpaRepository;
  private final GalleryItemJpaRepository galleryItemJpaRepository;
  private final OrderJpaRepository orderJpaRepository;
  private final PaymentAttemptJpaRepository paymentAttemptJpaRepository;
  private final RefundJpaRepository refundJpaRepository;
  private final SellerOnboardingJpaRepository sellerOnboardingJpaRepository;
  private final ReportJpaRepository reportJpaRepository;
  private final ServiceInquiryJpaRepository serviceInquiryJpaRepository;
  private final OperatorActionLogJpaRepository operatorActionLogJpaRepository;
  private final EntityManager entityManager;

  @Override
  @Transactional(readOnly = true)
  public long countUsers() {
    return userJpaRepository.count();
  }

  @Override
  @Transactional(readOnly = true)
  public long countUsersByRole(UserRole role) {
    return userJpaRepository.count((root, query, cb) -> cb.equal(root.get("role"), role));
  }

  @Override
  @Transactional(readOnly = true)
  public long countStores() {
    return storeJpaRepository.count();
  }

  @Override
  @Transactional(readOnly = true)
  public long countPendingOnboardings() {
    return sellerOnboardingJpaRepository.count(
        (root, query, cb) -> cb.equal(root.get("status"), SellerOnboardingStatus.PENDING));
  }

  @Override
  @Transactional(readOnly = true)
  public long countOpenReports() {
    return reportJpaRepository.count((root, query, cb) ->
        root.get("status").in(List.of(ReportStatus.SUBMITTED, ReportStatus.REVIEWING)));
  }

  @Override
  @Transactional(readOnly = true)
  public long countOpenServiceInquiries() {
    return serviceInquiryJpaRepository.count((root, query, cb) ->
        root.get("status").in(List.of(ServiceInquiryStatus.OPEN, ServiceInquiryStatus.ANSWERED)));
  }

  @Override
  @Transactional(readOnly = true)
  public long sumSucceededPaymentAmount(Instant startInclusive, Instant endExclusive) {
    return entityManager
        .createQuery("""
            select coalesce(sum(p.amount), 0)
            from PaymentAttempt p
            where p.status = :status
              and p.completedAt >= :startInclusive
              and p.completedAt < :endExclusive
            """, Long.class)
        .setParameter("status", PaymentAttemptStatus.SUCCEEDED)
        .setParameter("startInclusive", startInclusive)
        .setParameter("endExclusive", endExclusive)
        .getSingleResult();
  }

  @Override
  @Transactional(readOnly = true)
  public long sumCompletedRefundAmount(Instant startInclusive, Instant endExclusive) {
    return entityManager
        .createQuery("""
            select coalesce(sum(r.amount), 0)
            from Refund r
            where r.status = :status
              and r.completedAt >= :startInclusive
              and r.completedAt < :endExclusive
            """, Long.class)
        .setParameter("status", RefundStatus.COMPLETED)
        .setParameter("startInclusive", startInclusive)
        .setParameter("endExclusive", endExclusive)
        .getSingleResult();
  }

  @Override
  @Transactional(readOnly = true)
  public List<StatusCountResult> countOrdersByStatus() {
    return entityManager.createQuery("""
            select new io.point3.p3api.operator.application.result.StatusCountResult(
              cast(o.status as string), count(o)
            )
            from Order o
            group by o.status
            """, StatusCountResult.class).getResultList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<StatusCountResult> countPaymentsByStatus() {
    return entityManager.createQuery("""
            select new io.point3.p3api.operator.application.result.StatusCountResult(
              cast(p.status as string), count(p)
            )
            from PaymentAttempt p
            group by p.status
            """, StatusCountResult.class).getResultList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<StatusCountResult> countRefundsByStatus() {
    return entityManager.createQuery("""
            select new io.point3.p3api.operator.application.result.StatusCountResult(
              cast(r.status as string), count(r)
            )
            from Refund r
            group by r.status
            """, StatusCountResult.class).getResultList();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<User> findUsers(String keyword, UserRole role, UserStatus status, Pageable pageable) {
    Specification<User> specification = Specification.allOf(
        this.<User>contains("email", keyword).or(this.<User>contains("name", keyword)),
        this.<User>equalsValue("role", role),
        this.<User>equalsValue("status", status));
    return userJpaRepository.findAll(specification, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findUser(UUID userId) {
    return userJpaRepository.findById(userId);
  }

  @Override
  public User saveUser(User user) {
    return userJpaRepository.save(user);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Store> findStores(String keyword, StoreStatus status, Pageable pageable) {
    Specification<Store> specification = Specification.allOf(
        this.<Store>contains("name", keyword).or(this.<Store>contains("slug", keyword)),
        this.<Store>equalsValue("status", status));
    return storeJpaRepository.findAll(specification, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Store> findStore(UUID storeId) {
    return storeJpaRepository.findById(storeId);
  }

  @Override
  public Store saveStore(Store store) {
    return storeJpaRepository.save(store);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<StoreGalleryItem> findGalleryItems(
      UUID storeId, UUID assetId, StoreGalleryItemStatus status, Pageable pageable) {
    Specification<StoreGalleryItem> specification = Specification.allOf(
        this.<StoreGalleryItem>equalsValue("storeId", storeId),
        this.<StoreGalleryItem>equalsValue("assetId", assetId),
        this.<StoreGalleryItem>equalsValue("status", status));
    return galleryItemJpaRepository.findAll(specification, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<StoreGalleryItem> findGalleryItem(UUID galleryItemId) {
    return galleryItemJpaRepository.findById(galleryItemId);
  }

  @Override
  public StoreGalleryItem saveGalleryItem(StoreGalleryItem item) {
    return galleryItemJpaRepository.save(item);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Order> findOrders(
      UUID storeId,
      UUID buyerUserId,
      OrderStatus status,
      Instant startInclusive,
      Instant endExclusive,
      Pageable pageable) {
    Specification<Order> specification = Specification.allOf(
        this.<Order>equalsValue("storeId", storeId),
        this.<Order>equalsValue("buyerUserId", buyerUserId),
        this.<Order>equalsValue("status", status),
        this.<Order>greaterThanOrEqual("createdAt", startInclusive),
        this.<Order>lessThan("createdAt", endExclusive));
    return orderJpaRepository.findAll(specification, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Order> findOrder(UUID orderId) {
    return orderJpaRepository.findById(orderId);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PaymentAttempt> findPayments(
      PaymentAttemptStatus status,
      Instant startInclusive,
      Instant endExclusive,
      Pageable pageable) {
    Specification<PaymentAttempt> specification = Specification.allOf(
        this.<PaymentAttempt>equalsValue("status", status),
        this.<PaymentAttempt>greaterThanOrEqual("createdAt", startInclusive),
        this.<PaymentAttempt>lessThan("createdAt", endExclusive));
    return paymentAttemptJpaRepository.findAll(specification, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PaymentAttempt> findPayment(UUID paymentAttemptId) {
    return paymentAttemptJpaRepository.findById(paymentAttemptId);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Refund> findRefunds(
      RefundStatus status, Instant startInclusive, Instant endExclusive, Pageable pageable) {
    Specification<Refund> specification = Specification.allOf(
        this.<Refund>equalsValue("status", status),
        this.<Refund>greaterThanOrEqual("createdAt", startInclusive),
        this.<Refund>lessThan("createdAt", endExclusive));
    return refundJpaRepository.findAll(specification, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Refund> findRefund(UUID refundId) {
    return refundJpaRepository.findById(refundId);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<SellerOnboarding> findOnboardings(SellerOnboardingStatus status, Pageable pageable) {
    Specification<SellerOnboarding> specification =
        this.<SellerOnboarding>equalsValue("status", status);
    return sellerOnboardingJpaRepository.findAll(specification, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<SellerOnboarding> findOnboarding(UUID onboardingId) {
    return sellerOnboardingJpaRepository.findById(onboardingId);
  }

  @Override
  public SellerOnboarding saveOnboarding(SellerOnboarding onboarding) {
    return sellerOnboardingJpaRepository.save(onboarding);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Report> findReports(
      ReportStatus status, ReportTargetType targetType, String keyword, Pageable pageable) {
    Specification<Report> specification = Specification.allOf(
        this.<Report>equalsValue("status", status),
        this.<Report>equalsValue("targetType", targetType),
        this.<Report>contains("reason", keyword).or(this.<Report>contains("evidence", keyword)));
    return reportJpaRepository.findAll(specification, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Report> findReport(UUID reportId) {
    return reportJpaRepository.findById(reportId);
  }

  @Override
  public Report saveReport(Report report) {
    return reportJpaRepository.save(report);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ServiceInquiry> findServiceInquiries(
      ServiceInquiryStatus status, UUID assigneeOperatorId, String keyword, Pageable pageable) {
    Specification<ServiceInquiry> specification = Specification.allOf(
        this.<ServiceInquiry>equalsValue("status", status),
        this.<ServiceInquiry>equalsValue("assigneeOperatorId", assigneeOperatorId),
        this.<ServiceInquiry>contains("title", keyword)
            .or(this.<ServiceInquiry>contains("body", keyword)));
    return serviceInquiryJpaRepository.findAll(specification, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ServiceInquiry> findServiceInquiry(UUID serviceInquiryId) {
    return serviceInquiryJpaRepository.findById(serviceInquiryId);
  }

  @Override
  public ServiceInquiry saveServiceInquiry(ServiceInquiry inquiry) {
    return serviceInquiryJpaRepository.save(inquiry);
  }

  @Override
  public OperatorActionLog saveActionLog(OperatorActionLog actionLog) {
    return operatorActionLogJpaRepository.save(actionLog);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OperatorActionLog> findActionLogs(
      UUID operatorUserId,
      OperatorActionType actionType,
      OperatorTargetType targetType,
      UUID targetId,
      Instant startInclusive,
      Instant endExclusive,
      Pageable pageable) {
    Specification<OperatorActionLog> specification = Specification.allOf(
        this.<OperatorActionLog>equalsValue("operatorUserId", operatorUserId),
        this.<OperatorActionLog>equalsValue("actionType", actionType),
        this.<OperatorActionLog>equalsValue("targetType", targetType),
        this.<OperatorActionLog>equalsValue("targetId", targetId),
        this.<OperatorActionLog>greaterThanOrEqual("createdAt", startInclusive),
        this.<OperatorActionLog>lessThan("createdAt", endExclusive));
    return operatorActionLogJpaRepository.findAll(specification, pageable);
  }

  private <T> Specification<T> contains(String field, String keyword) {
    return (root, query, cb) -> {
      if (isBlank(keyword)) {
        return cb.conjunction();
      }

      String pattern = "%" + keyword.toLowerCase() + "%";
      return cb.like(cb.lower(root.get(field).as(String.class)), pattern);
    };
  }

  private <T> Specification<T> equalsValue(String field, Object value) {
    return (root, query, cb) -> {
      if (value == null) {
        return cb.conjunction();
      }
      return cb.equal(root.get(field), value);
    };
  }

  private <T> Specification<T> greaterThanOrEqual(String field, Instant value) {
    return (root, query, cb) -> {
      if (value == null) {
        return cb.conjunction();
      }
      return cb.greaterThanOrEqualTo(root.get(field), value);
    };
  }

  private <T> Specification<T> lessThan(String field, Instant value) {
    return (root, query, cb) -> {
      if (value == null) {
        return cb.conjunction();
      }
      return cb.lessThan(root.get(field), value);
    };
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
