package io.point3.p3api.order.application.query.order;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderErrorCode;
import io.point3.p3api.order.application.port.OrderPersistencePort;
import io.point3.p3api.order.application.result.OrderResult;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService implements OrderQueryUseCase {
  private final OrderPersistencePort orderPersistencePort;

  @Override
  public List<OrderResult> getBuyerOrders(UUID buyerUserId) {
    return orderPersistencePort.findAllByBuyerUserId(buyerUserId).stream()
        .map(OrderResult::from)
        .toList();
  }

  @Override
  public OrderResult getBuyerOrder(UUID orderId, UUID buyerUserId) {
    return OrderResult.from(orderPersistencePort
        .findByIdAndBuyerUserId(orderId, buyerUserId)
        .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND)));
  }

  @Override
  public List<OrderResult> getSellerOrders(UUID storeId) {
    return orderPersistencePort.findAllByStoreId(storeId).stream()
        .map(OrderResult::from)
        .toList();
  }

  @Override
  public OrderResult getSellerOrder(UUID orderId, UUID storeId) {
    return OrderResult.from(orderPersistencePort
        .findByIdAndStoreId(orderId, storeId)
        .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND)));
  }
}
