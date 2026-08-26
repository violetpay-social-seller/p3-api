package io.point3.p3api.operator.application.result;

import io.point3.p3api.operator.domain.entity.ServiceInquiry;
import io.point3.p3api.operator.domain.type.ServiceInquiryStatus;
import java.time.Instant;
import java.util.UUID;

public record OperatorServiceInquiryResult(
    UUID id,
    UUID requesterUserId,
    String title,
    String body,
    ServiceInquiryStatus status,
    UUID assigneeOperatorId,
    String answer,
    Instant answeredAt,
    Instant closedAt,
    Instant createdAt,
    Instant updatedAt) {

  public static OperatorServiceInquiryResult from(ServiceInquiry inquiry) {
    return new OperatorServiceInquiryResult(
        inquiry.getId(),
        inquiry.getRequesterUserId(),
        inquiry.getTitle(),
        inquiry.getBody(),
        inquiry.getStatus(),
        inquiry.getAssigneeOperatorId(),
        inquiry.getAnswer(),
        inquiry.getAnsweredAt(),
        inquiry.getClosedAt(),
        inquiry.getCreatedAt(),
        inquiry.getUpdatedAt());
  }
}
