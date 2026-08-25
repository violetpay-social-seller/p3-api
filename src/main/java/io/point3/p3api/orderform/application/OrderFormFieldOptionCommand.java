package io.point3.p3api.orderform.application;

public record OrderFormFieldOptionCommand(
    String label, String value, boolean active, int sortOrder) {}
