package io.point3.p3api.order.infrastructure.persistence;

import io.point3.p3api.order.application.port.OrderPersistencePort;
import io.point3.p3api.order.application.result.OrderPickupDateCount;
import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.type.OrderStatus;
import io.point3.p3api.payment.domain.type.PaymentAttemptStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderPersistencePort {

  private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

  private final OrderJpaRepository orderJpaRepository;

  @Override
  public Order save(Order order) {
    return orderJpaRepository.save(order);
  }

  @Override
  public Optional<Order> findById(UUID orderId) {
    return orderJpaRepository.findById(orderId);
  }

  @Override
  public Optional<Order> findByIdAndBuyerUserId(UUID orderId, UUID buyerUserId) {
    return orderJpaRepository.findByIdAndBuyerUserId(orderId, buyerUserId);
  }

  @Override
  public Optional<Order> findByIdAndStoreId(UUID orderId, UUID storeId) {
    return orderJpaRepository.findByIdAndStoreId(orderId, storeId);
  }

  @Override
  public Optional<Order> findByPaymentAttemptId(UUID paymentAttemptId) {
    return orderJpaRepository.findByPaymentAttemptId(paymentAttemptId);
  }

  @Override
  public List<Order> findAllByBuyerUserId(UUID buyerUserId) {
    return orderJpaRepository.findAllByBuyerUserIdOrderByCreatedAtDesc(buyerUserId);
  }

  @Override
  public List<Order> findAllByStoreId(UUID storeId) {
    return orderJpaRepository.findAllByStoreIdOrderByCreatedAtDesc(storeId);
  }

  @Override
  public List<OrderPickupDateCount> countByStoreIdAndPickupAtBetween(
      UUID storeId, Instant fromInclusive, Instant toExclusive, Set<OrderStatus> statuses) {

    Map<LocalDate, Long> counts =
        orderJpaRepository
            .countByStoreIdAndPickupAtBetween(storeId, fromInclusive, toExclusive, statuses)
            .stream()
            .collect(java.util.stream.Collectors.groupingBy(
                count -> count.getPickupAt().atZone(KOREA_ZONE_ID).toLocalDate(),
                java.util.stream.Collectors.summingLong(
                    OrderJpaRepository.PickupAtOrderCount::getOrderCount)));

    return counts.entrySet().stream()
        .map(entry -> new OrderPickupDateCount(entry.getKey(), entry.getValue()))
        .sorted(java.util.Comparator.comparing(OrderPickupDateCount::pickupDate))
        .toList();
  }

  @Override
  public List<Order> findCalendarOrders(
      UUID storeId, Instant startInclusive, Instant endExclusive) {
    return orderJpaRepository.findCalendarOrders(storeId, startInclusive, endExclusive);
  }

  @Override
  public List<Order> findCalendarOrdersByStatus(
      UUID storeId, OrderStatus status, Instant startInclusive, Instant endExclusive) {
    return orderJpaRepository.findCalendarOrdersByStatus(
        storeId, status, startInclusive, endExclusive);
  }

  @Override
  public long sumSucceededPaymentAmount(
      UUID storeId, Instant startInclusive, Instant endExclusive) {
    return orderJpaRepository.sumSucceededPaymentAmount(
        storeId, PaymentAttemptStatus.SUCCEEDED, startInclusive, endExclusive);
  }

  @Override
  public long countByStoreIdAndStatus(UUID storeId, OrderStatus status) {
    return orderJpaRepository.countByStoreIdAndStatus(storeId, status);
  }

  @Override
  public long countByStoreIdAndStatuses(UUID storeId, Collection<OrderStatus> statuses) {
    return orderJpaRepository.countByStoreIdAndStatusIn(storeId, statuses);
  }
}
