package io.point3.p3api.store.domain.entity;

import io.point3.p3api.store.domain.type.StoreStatus;
import io.point3.p3api.user.domain.type.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StoreStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private Store(String name) {
        this.name = name;
        this.status = StoreStatus.ACTIVE;
    }

    public static Store create(String name) {
        return new Store(name);
    }

    public boolean isActive() {
        return this.status == StoreStatus.ACTIVE;
    }

    public void inactive() {
        ensureActive("Only active store can inactive");
        this.status = StoreStatus.INACTIVE;
    }

    public void suspend() {
        ensureActive("Only active store can inactive");
        this.status = StoreStatus.SUSPENDED;
    }

    public void delete() {
        this.status = StoreStatus.DELETED;
    }

    private void ensureActive(String message) {
        if (!isActive()) {
            throw new IllegalArgumentException(message); // TODO:Store 도메인 예외로 변경 필요
        }
    }
}
