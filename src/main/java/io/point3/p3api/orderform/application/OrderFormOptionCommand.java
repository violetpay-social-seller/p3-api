package io.point3.p3api.orderform.application;

import io.point3.p3api.orderform.domain.type.OptionInputType;

public record OrderFormOptionCommand(
    String label,
    String value,
    OptionInputType inputType,
    Long price,
    String priceLabel,
    String settings,
    boolean active,
    int sortOrder) {}
