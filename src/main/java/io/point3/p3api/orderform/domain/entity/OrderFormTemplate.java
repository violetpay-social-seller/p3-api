package io.point3.p3api.orderform.domain.entity;

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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "order_form_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderFormTemplate {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "store_id", nullable = false)
  private UUID storeId;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "active", nullable = false)
  private boolean active;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  private OrderFormTemplate(UUID storeId, String name) {
    this.storeId = storeId;
    this.name = name;
    this.active = true;
  }

  public static OrderFormTemplate create(UUID storeId, String name) {
    Objects.requireNonNull(storeId, "storeId");
    Objects.requireNonNull(name, "name");

    return new OrderFormTemplate(storeId, name);
  }

  public void inactive() {
    this.active = false;
  }
}
