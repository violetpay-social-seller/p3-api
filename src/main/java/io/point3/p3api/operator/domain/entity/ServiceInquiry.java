package io.point3.p3api.operator.domain.entity;

import io.point3.p3api.operator.domain.type.ServiceInquiryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "service_inquiries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceInquiry {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "requester_user_id")
  private UUID requesterUserId;

  @Column(name = "title", nullable = false, length = 150)
  private String title;

  @Column(name = "body", nullable = false, columnDefinition = "text")
  private String body;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private ServiceInquiryStatus status;

  @Column(name = "assignee_operator_id")
  private UUID assigneeOperatorId;

  @Column(name = "answer", columnDefinition = "text")
  private String answer;

  @Column(name = "answered_at")
  private Instant answeredAt;

  @Column(name = "closed_at")
  private Instant closedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  private ServiceInquiry(UUID requesterUserId, String title, String body) {
    this.requesterUserId = requesterUserId;
    this.title = title;
    this.body = body;
    this.status = ServiceInquiryStatus.OPEN;
  }

  public static ServiceInquiry create(UUID requesterUserId, String title, String body) {
    Objects.requireNonNull(title, "title");
    Objects.requireNonNull(body, "body");
    if (title.isBlank()) {
      throw new IllegalArgumentException("title must not be blank");
    }
    if (body.isBlank()) {
      throw new IllegalArgumentException("body must not be blank");
    }

    return new ServiceInquiry(requesterUserId, title, body);
  }

  public void assign(UUID operatorUserId) {
    this.assigneeOperatorId = Objects.requireNonNull(operatorUserId, "operatorUserId");
  }

  public void answer(UUID operatorUserId, String answer, Instant answeredAt) {
    Objects.requireNonNull(operatorUserId, "operatorUserId");
    Objects.requireNonNull(answer, "answer");
    Objects.requireNonNull(answeredAt, "answeredAt");
    if (answer.isBlank()) {
      throw new IllegalArgumentException("answer must not be blank");
    }

    this.assigneeOperatorId = operatorUserId;
    this.answer = answer;
    this.answeredAt = answeredAt;
    this.status = ServiceInquiryStatus.ANSWERED;
  }

  public void close(UUID operatorUserId, Instant closedAt) {
    Objects.requireNonNull(operatorUserId, "operatorUserId");
    Objects.requireNonNull(closedAt, "closedAt");

    this.assigneeOperatorId = operatorUserId;
    this.closedAt = closedAt;
    this.status = ServiceInquiryStatus.CLOSED;
  }
}
