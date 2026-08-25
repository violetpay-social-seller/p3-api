package io.point3.p3api.user.domain.entity;

import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.domain.type.UserStatus;
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
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "cognito_sub", nullable = false, unique = true, updatable = false, length = 128)
  private String cognitoSub;

  @Column(name = "email", nullable = false, length = 320)
  private String email;

  @Column(name = "payer_id", length = 128)
  private String payerId;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 30)
  private UserRole role;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private UserStatus status;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  private User(String cognitoSub, String email, String name, UserRole role) {
    this.cognitoSub = cognitoSub;
    this.email = email;
    this.name = name;
    this.role = role;
    this.status = UserStatus.ACTIVE;
  }

  public static User create(String cognitoSub, String email, String name, UserRole role) {
    Objects.requireNonNull(cognitoSub, "cognitoSub");
    Objects.requireNonNull(email, "email");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(role, "role");

    return new User(cognitoSub, email, name, role);
  }

  public void updateProfile(String email, String name) {
    this.email = email;
    this.name = name;
  }

  public void connectPayer(String payerId) {
    Objects.requireNonNull(payerId, "payerId");
    if (payerId.isBlank()) {
      throw new IllegalArgumentException("payerId must not be blank");
    }
    this.payerId = payerId;
  }

  public void withdraw() {
    ensureActive("Only active users can withdraw");
    this.status = UserStatus.WITHDRAWN;
  }

  public void ban() {
    ensureActive("Only active users can be banned");
    this.status = UserStatus.BANNED;
  }

  public boolean isActive() {
    return this.status == UserStatus.ACTIVE;
  }

  private void ensureActive(String message) {
    if (!isActive()) {
      throw new IllegalArgumentException(message); // TODO:User 도메인 예외로 변경 필요
    }
  }
}
