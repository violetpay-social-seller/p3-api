package io.point3.p3api.orderform.controller.request;

import io.point3.p3api.orderform.domain.type.SelectionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderFormOptionGroupRequest(
    @NotBlank @Size(max = 150) String label,
    @NotNull SelectionType selectionType,
    boolean required,
    @PositiveOrZero int sortOrder,
    @Valid @NotNull List<OrderFormOptionRequest> options) {

  public OrderFormOptionGroupRequest {
    options = options == null ? List.of() : List.copyOf(options);
  }
}
