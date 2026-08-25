package io.point3.p3api.store.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "store_holidays")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreHoliday {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "store_id", nullable = false)
  private UUID storeId;

  @Column(name = "holiday_date", nullable = false)
  private LocalDate holidayDate;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  private StoreHoliday(UUID storeId, LocalDate holidayDate) {
    this.storeId = storeId;
    this.holidayDate = holidayDate;
  }

  public static StoreHoliday create(UUID storeId, LocalDate holidayDate) {
    Objects.requireNonNull(storeId, "storeId");
    Objects.requireNonNull(holidayDate, "holidayDate");

    return new StoreHoliday(storeId, holidayDate);
  }
}
