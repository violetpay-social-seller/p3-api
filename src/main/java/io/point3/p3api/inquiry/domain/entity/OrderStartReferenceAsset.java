package io.point3.p3api.inquiry.domain.entity;

import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
    name = "order_start_reference_assets",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_order_start_reference_assets_inquiry_sort_order",
            columnNames = {"inquiry_id", "sort_order"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStartReferenceAsset {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "inquiry_id", nullable = false)
  private UUID inquiryId;

  @Column(name = "submitted_by", nullable = false)
  private UUID submittedBy;

  @Column(name = "asset_id", nullable = false)
  private UUID assetId;

  @Enumerated(EnumType.STRING)
  @Column(name = "source", nullable = false, length = 30)
  private OrderFormReferenceAssetSource source;

  @Column(name = "snapshot", nullable = false, columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String snapshot;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  private OrderStartReferenceAsset(
      UUID inquiryId,
      UUID submittedBy,
      UUID assetId,
      OrderFormReferenceAssetSource source,
      String snapshot,
      int sortOrder) {
    this.inquiryId = inquiryId;
    this.submittedBy = submittedBy;
    this.assetId = assetId;
    this.source = source;
    this.snapshot = snapshot;
    this.sortOrder = sortOrder;
  }

  public static OrderStartReferenceAsset create(
      UUID inquiryId,
      UUID submittedBy,
      UUID assetId,
      OrderFormReferenceAssetSource source,
      String snapshot,
      int sortOrder) {
    Objects.requireNonNull(inquiryId, "inquiryId");
    Objects.requireNonNull(submittedBy, "submittedBy");
    Objects.requireNonNull(assetId, "assetId");
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(snapshot, "snapshot");

    if (sortOrder < 0) {
      throw new IllegalArgumentException("sortOrder must be greater than or equal to 0");
    }

    return new OrderStartReferenceAsset(
        inquiryId, submittedBy, assetId, source, snapshot, sortOrder);
  }
}
