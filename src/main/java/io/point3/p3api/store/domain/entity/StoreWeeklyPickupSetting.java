package io.point3.p3api.store.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "store_weekly_pickup_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreWeeklyPickupSetting {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "store_id", nullable = false)
  private UUID storeId;

  @Enumerated(EnumType.STRING)
  @Column(name = "day_of_week", nullable = false, length = 10)
  private DayOfWeek dayOfWeek;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  @Column(name = "daily_order_capacity", nullable = false)
  private int dailyOrderCapacity;

  @Column(nullable = false)
  private boolean enabled;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  private StoreWeeklyPickupSetting(
      UUID storeId,
      DayOfWeek dayOfWeek,
      LocalTime startTime,
      LocalTime endTime,
      int dailyOrderCapacity,
      boolean enabled) {
    this.storeId = storeId;
    this.dayOfWeek = dayOfWeek;
    this.startTime = startTime;
    this.endTime = endTime;
    this.dailyOrderCapacity = dailyOrderCapacity;
    this.enabled = enabled;
  }

  public static StoreWeeklyPickupSetting create(
      UUID storeId,
      DayOfWeek dayOfWeek,
      LocalTime startTime,
      LocalTime endTime,
      int dailyOrderCapacity,
      boolean enabled) {
    Objects.requireNonNull(storeId, "storeId");
    Objects.requireNonNull(dayOfWeek, "dayOfWeek");
    Objects.requireNonNull(startTime, "startTime");
    Objects.requireNonNull(endTime, "endTime");

    return new StoreWeeklyPickupSetting(
        storeId, dayOfWeek, startTime, endTime, dailyOrderCapacity, enabled);
  }
}
