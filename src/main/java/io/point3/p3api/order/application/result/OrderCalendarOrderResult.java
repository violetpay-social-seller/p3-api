package io.point3.p3api.order.application.result;

import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCalendarOrderResult(
    UUID orderId,
    UUID inquiryId,
    UUID buyerUserId,
    String orderNumber,
    String menuName,
    long paidAmount,
    Instant pickupAt,
    LocalDate pickupDate,
    LocalTime pickupTime,
    OrderStatus status,
    List<OrderReferenceAssetResult> referenceAssets) {

  public OrderCalendarOrderResult {
    referenceAssets = List.copyOf(referenceAssets);
  }

  public static OrderCalendarOrderResult from(
      Order order, ZoneId zoneId, List<OrderReferenceAssetResult> referenceAssets) {
    ZonedDateTime pickupDateTime = order.getPickupAt().atZone(zoneId);

    return new OrderCalendarOrderResult(
        order.getId(),
        order.getInquiryId(),
        order.getBuyerUserId(),
        order.getOrderNumber(),
        order.getMenuNameSnapshot(),
        order.getPaidAmount(),
        order.getPickupAt(),
        pickupDateTime.toLocalDate(),
        pickupDateTime.toLocalTime(),
        order.getStatus(),
        referenceAssets);
  }

  public static OrderCalendarOrderResult from(Order order, ZoneId zoneId) {
    return from(order, zoneId, List.of());
  }

  @Override
  public List<OrderReferenceAssetResult> referenceAssets() {
    return List.copyOf(referenceAssets);
  }
}
