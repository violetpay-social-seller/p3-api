package io.point3.p3api.orderform.application.result;

import io.point3.p3api.orderform.domain.entity.OrderFormOption;
import io.point3.p3api.orderform.domain.type.OptionInputType;
import java.util.UUID;

public record OrderFormOptionResult(
    UUID id,
    String label,
    String value,
    OptionInputType inputType,
    Long price,
    String priceLabel,
    String settings,
    boolean active,
    int sortOrder) {

  public OrderFormOptionResult(
      UUID id,
      String label,
      String value,
      OptionInputType inputType,
      Long price,
      String settings,
      boolean active,
      int sortOrder) {
    this(id, label, value, inputType, price, null, settings, active, sortOrder);
  }

  public static OrderFormOptionResult from(OrderFormOption option) {
    return new OrderFormOptionResult(
        option.getId(),
        option.getLabel(),
        option.getValue(),
        option.getInputType(),
        option.getPrice(),
        option.getPriceLabel(),
        option.getSettings(),
        option.isActive(),
        option.getSortOrder());
  }
}
