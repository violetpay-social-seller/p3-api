package io.point3.p3api.orderform.application.create;

import io.point3.p3api.orderform.application.OrderFormFieldCommand;
import io.point3.p3api.orderform.application.OrderFormFieldOptionCommand;
import io.point3.p3api.orderform.domain.type.FieldType;
import io.point3.p3api.orderform.domain.type.OrderFormCategory;
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
      String label,
      FieldType fieldType,
      boolean required,
      Long price,
      String settings,
      int sortOrder,
      OrderFormCategory groupCategory,
      String groupTitle,
      String groupDescription,
      int groupSortOrder,
      List<OrderFormFieldOptionCommand> options)
      implements OrderFormFieldCommand {

    public Field {
      options = options == null ? List.of() : List.copyOf(options);
    }

    public Field(
        String label, FieldType fieldType, boolean required, String settings, int sortOrder) {
      this(
          label,
          fieldType,
          required,
          null,
          settings,
          sortOrder,
          OrderFormCategory.DESIGN,
          OrderFormCategory.DESIGN.getTitle(),
          null,
          0,
          List.of());
    }
  }
}
