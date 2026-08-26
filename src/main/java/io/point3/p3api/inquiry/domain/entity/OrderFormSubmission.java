package io.point3.p3api.inquiry.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "order_form_submissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderFormSubmission {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "inquiry_id", nullable = false)
  private UUID inquiryId;

  @Column(name = "template_id", nullable = false)
  private UUID templateId;

  @Column(name = "submitted_by", nullable = false)
  private UUID submittedBy;

  @Column(name = "answers", nullable = false, columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String answers;

  @Column(name = "reference_assets", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String referenceAssets;

  @Column(name = "cancellation_refund_agreed", nullable = false)
  private boolean cancellationRefundAgreed;

  @CreationTimestamp
  @Column(name = "submitted_at", nullable = false, updatable = false)
  private Instant submittedAt;

  private OrderFormSubmission(
      UUID inquiryId,
      UUID templateId,
      UUID submittedBy,
      String answers,
      String referenceAssets,
      boolean cancellationRefundAgreed) {
    this.inquiryId = inquiryId;
    this.templateId = templateId;
    this.submittedBy = submittedBy;
    this.answers = answers;
    this.referenceAssets = referenceAssets;
    this.cancellationRefundAgreed = cancellationRefundAgreed;
  }

  public static OrderFormSubmission create(
      UUID inquiryId,
      UUID templateId,
      UUID submittedBy,
      String answers,
      String referenceAssets,
      boolean cancellationRefundAgreed) {
    Objects.requireNonNull(inquiryId, "inquiryId");
    Objects.requireNonNull(templateId, "templateId");
    Objects.requireNonNull(submittedBy, "submittedBy");
    Objects.requireNonNull(answers, "answers");

    return new OrderFormSubmission(
        inquiryId, templateId, submittedBy, answers, referenceAssets, cancellationRefundAgreed);
  }
}
