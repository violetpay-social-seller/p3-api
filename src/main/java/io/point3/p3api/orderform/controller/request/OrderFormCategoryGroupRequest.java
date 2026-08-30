package io.point3.p3api.orderform.controller.request;

import io.point3.p3api.orderform.domain.type.OrderFormCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderFormCategoryGroupRequest(
    @NotNull OrderFormCategory category,
    @NotBlank @Size(max = 100) String title,
    String description,
    @PositiveOrZero int sortOrder,
    @Valid @NotEmpty List<OrderFormOptionGroupRequest> optionGroups) {

  public OrderFormCategoryGroupRequest {
    optionGroups = optionGroups == null ? null : List.copyOf(optionGroups);
  }

  @Override
  public List<OrderFormOptionGroupRequest> optionGroups() {
    return optionGroups == null ? null : List.copyOf(optionGroups);
  }
}
