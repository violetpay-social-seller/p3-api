package io.point3.p3api.orderform.application.update;

import io.point3.p3api.orderform.application.OrderFormOptionCommand;
import io.point3.p3api.orderform.application.OrderFormOptionGroupCommand;
import io.point3.p3api.orderform.domain.type.OrderFormCategory;
import io.point3.p3api.orderform.domain.type.SelectionType;
import java.util.List;
import java.util.UUID;

public record UpdateOrderFormCommand(
    UUID storeId, UUID templateId, String name, List<OptionGroup> optionGroups) {

  public UpdateOrderFormCommand {
    optionGroups = List.copyOf(optionGroups);
  }

  @Override
  public List<OptionGroup> optionGroups() {
    return List.copyOf(optionGroups);
  }

  public record OptionGroup(
      String label,
      SelectionType selectionType,
      boolean required,
      int sortOrder,
      OrderFormCategory groupCategory,
      String groupTitle,
      String groupDescription,
      int groupSortOrder,
      List<OrderFormOptionCommand> options)
      implements OrderFormOptionGroupCommand {

    public OptionGroup {
      options = options == null ? List.of() : List.copyOf(options);
    }

    public OptionGroup(String label, SelectionType selectionType, boolean required, int sortOrder) {
      this(
          label,
          selectionType,
          required,
          sortOrder,
          OrderFormCategory.DESIGN,
          OrderFormCategory.DESIGN.getTitle(),
          null,
          0,
          List.of());
    }
  }
}
