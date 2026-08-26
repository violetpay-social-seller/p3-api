package io.point3.p3api.payment.application.port;

public record Point3RefundResult(boolean completed, String failureCode) {}
