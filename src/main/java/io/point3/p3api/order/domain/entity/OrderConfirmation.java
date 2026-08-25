package io.point3.p3api.order.domain.entity;

import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "order_confirmations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderConfirmation {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "inquiry_id", nullable = false)
  private UUID inquiryId;

  @Column(name = "order_form_submission_id")
  private UUID orderFormSubmissionId;

  @Column(name = "created_by", nullable = false)
  private UUID createdBy;

  @Column(name = "menu_name", nullable = false, length = 150)
  private String menuName;

  @Column(name = "option_summary", nullable = false, columnDefinition = "text")
  private String optionSummary;

  @Column(name = "amount", nullable = false)
  private long amount;

  @Column(name = "pickup_at", nullable = false)
  private Instant pickupAt;

  @Column(name = "store_name_snapshot", nullable = false, length = 100)
  private String storeNameSnapshot;

  @Column(name = "order_summary", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String orderSummary;

  @Column(name = "additional_items", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String additionalItems;

  @Column(name = "seller_note", columnDefinition = "text")
  private String sellerNote;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private OrderConfirmationStatus status;

  @Column(name = "sent_at")
  private Instant sentAt;

  @Column(name = "revision_requested_at")
  private Instant revisionRequestedAt;

  @Column(name = "buyer_viewed_at")
  private Instant buyerViewedAt;

  @Column(name = "replaced_by_confirmation_id")
  private UUID replacedByConfirmationId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  private OrderConfirmation(
      UUID inquiryId,
      UUID orderFormSubmissionId,
      UUID createdBy,
      String menuName,
      String optionSummary,
      long amount,
      Instant pickupAt,
      String storeNameSnapshot,
      String orderSummary,
      String additionalItems,
      String sellerNote) {
    this.inquiryId = inquiryId;
    this.orderFormSubmissionId = orderFormSubmissionId;
    this.createdBy = createdBy;
    this.menuName = menuName;
    this.optionSummary = optionSummary;
    this.amount = amount;
    this.pickupAt = pickupAt;
    this.storeNameSnapshot = storeNameSnapshot;
    this.orderSummary = orderSummary;
    this.additionalItems = additionalItems;
    this.sellerNote = sellerNote;
    this.status = OrderConfirmationStatus.DRAFT;
  }

  public static OrderConfirmation create(
      UUID inquiryId,
      UUID orderFormSubmissionId,
      UUID createdBy,
      String menuName,
      String optionSummary,
      long amount,
      Instant pickupAt,
      String storeNameSnapshot,
      String orderSummary,
      String additionalItems,
      String sellerNote) {
    Objects.requireNonNull(inquiryId, "inquiryId");
    Objects.requireNonNull(createdBy, "createdBy");
    Objects.requireNonNull(menuName, "menuName");
    Objects.requireNonNull(optionSummary, "optionSummary");
    Objects.requireNonNull(pickupAt, "pickupAt");
    Objects.requireNonNull(storeNameSnapshot, "storeNameSnapshot");

    if (amount < 0) {
      throw new IllegalArgumentException("amount must be greater than or equal to 0");
    }

    return new OrderConfirmation(
        inquiryId,
        orderFormSubmissionId,
        createdBy,
        menuName,
        optionSummary,
        amount,
        pickupAt,
        storeNameSnapshot,
        orderSummary,
        additionalItems,
        sellerNote);
  }

  public void sent(Instant sentAt) {
    Objects.requireNonNull(sentAt, "sentAt");
    this.status = OrderConfirmationStatus.SENT;
    this.sentAt = sentAt;
  }

  public void requestRevision(Instant revisionRequestedAt) {
    Objects.requireNonNull(revisionRequestedAt, "revisionRequestedAt");
    this.status = OrderConfirmationStatus.REVISION_REQUESTED;
    this.revisionRequestedAt = revisionRequestedAt;
  }

  public void replaceWith(UUID replacedByConfirmationId) {
    Objects.requireNonNull(replacedByConfirmationId, "replacedByConfirmationId");
    this.status = OrderConfirmationStatus.REPLACED;
    this.replacedByConfirmationId = replacedByConfirmationId;
  }

  public void markBuyerViewed(Instant viewedAt) {
    Objects.requireNonNull(viewedAt, "viewedAt");
    if (this.buyerViewedAt == null) {
      this.buyerViewedAt = viewedAt;
    }
  }

  public void markPaid() {
    this.status = OrderConfirmationStatus.PAID;
  }
}
