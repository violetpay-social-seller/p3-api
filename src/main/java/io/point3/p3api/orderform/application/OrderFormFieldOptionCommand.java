package io.point3.p3api.orderform.application;

public record OrderFormFieldOptionCommand(
    String label, String value, Long price, boolean active, int sortOrder) {}
