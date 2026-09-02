package io.point3.p3api.orderform.controller.response;

import io.point3.p3api.orderform.application.result.OrderFormOptionResult;
import io.point3.p3api.orderform.domain.type.OptionInputType;
import java.util.UUID;

public record OrderFormOptionResponse(
    UUID id,
    String label,
    String value,
    OptionInputType inputType,
    Long price,
    String priceLabel,
    String settings,
    boolean active,
    int sortOrder) {

  public static OrderFormOptionResponse from(OrderFormOptionResult result) {
    return new OrderFormOptionResponse(
        result.id(),
        result.label(),
        result.value(),
        result.inputType(),
        result.price(),
        result.priceLabel(),
        result.settings(),
        result.active(),
        result.sortOrder());
  }
}
