package io.point3.p3api.user.domain.entity;

import io.point3.p3api.user.domain.type.SignupProvider;
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

  @Column(name = "phone_number", length = 30)
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "signup_provider", length = 30)
  private SignupProvider signupProvider;

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

  private User(
      String cognitoSub,
      String email,
      String phoneNumber,
      SignupProvider signupProvider,
      String name,
      UserRole role) {
    this.cognitoSub = cognitoSub;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.signupProvider = signupProvider;
    this.name = name;
    this.role = role;
    this.status = UserStatus.ACTIVE;
  }

  public static User create(
      String cognitoSub,
      String email,
      String name,
      UserRole role,
      String phoneNumber,
      SignupProvider signupProvider) {
    Objects.requireNonNull(cognitoSub, "cognitoSub");
    Objects.requireNonNull(email, "email");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(role, "role");
    if (role != UserRole.OPERATOR) {
      Objects.requireNonNull(phoneNumber, "phoneNumber");
      Objects.requireNonNull(signupProvider, "signupProvider");
      if (phoneNumber.isBlank()) {
        throw new IllegalArgumentException("phoneNumber must not be blank");
      }
    }

    return new User(cognitoSub, email, phoneNumber, signupProvider, name, role);
  }

  public void updateProfile(String email, String name) {
    Objects.requireNonNull(email, "email");
    Objects.requireNonNull(name, "name");
    if (email.isBlank()) {
      throw new IllegalArgumentException("email must not be blank");
    }
    if (name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }

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

  public void unban() {
    if (this.status != UserStatus.BANNED) {
      throw new IllegalArgumentException("Only banned users can be unbanned");
    }
    this.status = UserStatus.ACTIVE;
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
