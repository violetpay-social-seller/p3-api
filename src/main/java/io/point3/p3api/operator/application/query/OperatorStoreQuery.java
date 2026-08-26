package io.point3.p3api.operator.application.query;

import io.point3.p3api.store.domain.type.StoreStatus;

public record OperatorStoreQuery(String keyword, StoreStatus status, OperatorPageQuery pageQuery) {}
