package io.point3.p3api.orderform.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "order_form_field_groups",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_order_form_field_groups_template_sort_order",
            columnNames = {"template_id", "sort_order"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderFormFieldGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "template_id", nullable = false)
  private UUID templateId;

  @Column(name = "title", nullable = false, length = 100)
  private String title;

  @Column(name = "description", columnDefinition = "text")
  private String description;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  private OrderFormFieldGroup(UUID templateId, String title, String description, int sortOrder) {
    this.templateId = templateId;
    this.title = title;
    this.description = description;
    this.sortOrder = sortOrder;
  }

  public static OrderFormFieldGroup create(
      UUID templateId, String title, String description, int sortOrder) {
    Objects.requireNonNull(templateId, "templateId");
    Objects.requireNonNull(title, "title");

    if (sortOrder < 0) {
      throw new IllegalArgumentException("sortOrder must be greater than or equal to 0");
    }

    return new OrderFormFieldGroup(templateId, title, description, sortOrder);
  }
}
