package io.point3.p3api.orderform.application.result;

import io.point3.p3api.orderform.domain.entity.OrderFormFieldOption;
import java.util.UUID;

public record OrderFormFieldOptionResult(
    UUID id, String label, String value, boolean active, int sortOrder) {

  public static OrderFormFieldOptionResult from(OrderFormFieldOption option) {
    return new OrderFormFieldOptionResult(
        option.getId(),
        option.getLabel(),
        option.getValue(),
        option.isActive(),
        option.getSortOrder());
  }
}
