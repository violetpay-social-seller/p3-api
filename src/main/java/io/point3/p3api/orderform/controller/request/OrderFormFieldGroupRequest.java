package io.point3.p3api.orderform.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderFormFieldGroupRequest(
    @NotBlank @Size(max = 100) String title,
    String description,
    @PositiveOrZero int sortOrder,
    @Valid @NotEmpty List<OrderFormFieldRequest> fields) {

  public OrderFormFieldGroupRequest {
    fields = fields == null ? null : List.copyOf(fields);
  }
}
