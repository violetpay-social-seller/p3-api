package io.point3.p3api.store.application.location.result;

public record StoreLocationResult(
    String name, String roadAddress, String jibunAddress, double latitude, double longitude) {}
