package io.point3.p3api.orderform.controller.request;

import io.point3.p3api.orderform.domain.type.FieldType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderFormFieldRequest(
    @NotBlank @Size(max = 150) String label,
    @NotNull FieldType fieldType,
    boolean required,
    String settings,
    @PositiveOrZero int sortOrder,
    @Valid List<OrderFormFieldOptionRequest> options) {

  public OrderFormFieldRequest {
    options = options == null ? List.of() : List.copyOf(options);
  }
}
