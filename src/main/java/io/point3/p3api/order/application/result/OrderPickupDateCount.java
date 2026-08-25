package io.point3.p3api.order.application.result;

import java.time.LocalDate;

public record OrderPickupDateCount(LocalDate pickupDate, long orderCount) {}
