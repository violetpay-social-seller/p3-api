package io.point3.p3api.operator.application.query;

import io.point3.p3api.operator.domain.type.ServiceInquiryStatus;
import java.util.UUID;

public record OperatorServiceInquiryQuery(
    ServiceInquiryStatus status,
    UUID assigneeOperatorId,
    String keyword,
    OperatorPageQuery pageQuery) {}
