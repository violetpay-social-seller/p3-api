package io.point3.p3api.order.application.result;

import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    OrderStatus status) {

  public static OrderCalendarOrderResult from(Order order, ZoneId zoneId) {
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
        order.getStatus());
  }
}
