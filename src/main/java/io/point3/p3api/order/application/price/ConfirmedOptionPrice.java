package io.point3.p3api.order.application.price;

import java.util.UUID;

public record ConfirmedOptionPrice(UUID optionGroupId, String optionValue, Long amount) {}
