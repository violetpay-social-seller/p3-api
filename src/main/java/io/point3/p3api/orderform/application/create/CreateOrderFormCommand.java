package io.point3.p3api.orderform.application.create;

import io.point3.p3api.orderform.application.OrderFormFieldCommand;
import io.point3.p3api.orderform.domain.type.FieldType;
import java.util.List;
import java.util.UUID;

public record CreateOrderFormCommand(UUID storeId, String name, List<Field> fields) {

  public CreateOrderFormCommand {
    fields = List.copyOf(fields);
  }

  @Override
  public List<Field> fields() {
    return List.copyOf(fields);
  }

  public record Field(
      String label, FieldType fieldType, boolean required, String settings, int sortOrder)
      implements OrderFormFieldCommand {}
}
