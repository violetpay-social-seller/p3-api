package io.point3.p3api.seller.domain.entity;

import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "seller_onboardings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerOnboarding {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "applicant_user_id", nullable = false, updatable = false)
  private UUID applicantUserId;

  @Column(name = "store_name", nullable = false, length = 100)
  private String storeName;

  @Column(name = "phone_number", nullable = false, length = 30)
  private String phoneNumber;

  @Column(name = "address", nullable = false, length = 255)
  private String address;

  @Column(name = "sns_link", length = 500)
  private String snsLink;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private SellerOnboardingStatus status;

  @Column(name = "rejection_reason", columnDefinition = "text")
  private String rejectionReason;

  @Column(name = "reviewed_by")
  private UUID reviewedBy;

  @Column(name = "reviewed_at")
  private Instant reviewedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  private SellerOnboarding(
      UUID applicantUserId, String storeName, String phoneNumber, String address, String snsLink) {
    this.applicantUserId = applicantUserId;
    this.storeName = storeName;
    this.phoneNumber = phoneNumber;
    this.address = address;
    this.snsLink = snsLink;
    this.status = SellerOnboardingStatus.PENDING;
  }

  public static SellerOnboarding create(
      UUID applicantUserId, String storeName, String phoneNumber, String address, String snsLink) {
    Objects.requireNonNull(applicantUserId, "applicantUserId");
    Objects.requireNonNull(storeName, "storeName");
    Objects.requireNonNull(phoneNumber, "phoneNumber");
    Objects.requireNonNull(address, "address");

    return new SellerOnboarding(applicantUserId, storeName, phoneNumber, address, snsLink);
  }

  public void hold(UUID reviewerId, String reason, Instant reviewedAt) {
    Objects.requireNonNull(reviewerId, "reviewerId");
    Objects.requireNonNull(reason, "reason");
    Objects.requireNonNull(reviewedAt, "reviewedAt");
    if (reason.isBlank()) {
      throw new IllegalArgumentException("reason must not be blank");
    }
    if (status != SellerOnboardingStatus.PENDING) {
      throw new IllegalStateException("Only pending onboarding can be held");
    }

    this.status = SellerOnboardingStatus.HELD;
    this.rejectionReason = reason;
    this.reviewedBy = reviewerId;
    this.reviewedAt = reviewedAt;
  }
}
