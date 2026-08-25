package io.point3.p3api.order.infrastructure.persistence;

import io.point3.p3api.order.application.port.OrderPersistencePort;
import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderPersistencePort {
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
}
