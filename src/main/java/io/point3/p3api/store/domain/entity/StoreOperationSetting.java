package io.point3.p3api.store.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "store_operation_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreOperationSetting {

  @Id
  @Column(name = "store_id")
  private UUID storeId;

  @Column(name = "lead_time_minutes", nullable = false)
  private int leadTimeMinutes;

  @Column(name = "cancellation_cutoff_days", nullable = false)
  private int cancellationCutoffDays;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  private StoreOperationSetting(UUID storeId, int leadTimeMinutes, int cancellationCutoffDays) {
    this.storeId = storeId;
    this.leadTimeMinutes = leadTimeMinutes;
    this.cancellationCutoffDays = cancellationCutoffDays;
  }

  public static StoreOperationSetting create(
      UUID storeId, int leadTimeMinutes, int cancellationCutoffDays) {
    Objects.requireNonNull(storeId, "storeId");

    return new StoreOperationSetting(storeId, leadTimeMinutes, cancellationCutoffDays);
  }

  public void update(int leadTimeMinutes, int cancellationCutoffDays) {
    this.leadTimeMinutes = leadTimeMinutes;
    this.cancellationCutoffDays = cancellationCutoffDays;
  }
}
