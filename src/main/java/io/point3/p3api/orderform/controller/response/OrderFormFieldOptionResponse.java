package io.point3.p3api.orderform.controller.response;

import io.point3.p3api.orderform.application.result.OrderFormFieldOptionResult;
import java.util.UUID;

public record OrderFormFieldOptionResponse(
    UUID id, String label, String value, long price, boolean active, int sortOrder) {

  public static OrderFormFieldOptionResponse from(OrderFormFieldOptionResult result) {
    return new OrderFormFieldOptionResponse(
        result.id(),
        result.label(),
        result.value(),
        result.price(),
        result.active(),
        result.sortOrder());
  }
}
