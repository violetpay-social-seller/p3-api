package io.point3.p3api.orderform.controller.request;

import io.point3.p3api.orderform.domain.type.OrderFormCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderFormFieldGroupRequest(
    @NotNull OrderFormCategory category,
    @NotBlank @Size(max = 100) String title,
    String description,
    @PositiveOrZero int sortOrder,
    @Valid @NotEmpty List<OrderFormFieldRequest> fields) {

  public OrderFormFieldGroupRequest {
    fields = fields == null ? null : List.copyOf(fields);
  }
}
