package io.point3.p3api.asset.domain.entity;

import io.point3.p3api.asset.domain.type.AssetStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "assets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Asset {

    @Id
    private UUID id;

    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "uploaded_by", nullable = false)
    private UUID uploadedBy;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size", nullable = false)
    private long size;

    @Column(name = "object_key", nullable = false, unique = true, length = 1024)
    private String objectKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AssetStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private Asset(
            UUID id,
            UUID storeId,
            UUID uploadedBy,
            String originalFilename,
            String contentType,
            long size,
            String objectKey) {
        this.id = id;
        this.storeId = storeId;
        this.uploadedBy = uploadedBy;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.objectKey = objectKey;
        this.status = AssetStatus.UPLOADED;
    }

    public static Asset create(
            UUID id,
            UUID storeId,
            UUID uploadedBy,
            String originalFilename,
            String contentType,
            long size,
            String objectKey) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(uploadedBy, "uploadedBy");
        Objects.requireNonNull(originalFilename, "originalFilename");
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(objectKey, "objectKey");

        if (size < 0) {
            throw new IllegalArgumentException("size must be greater than or equal to 0");
        }

        return new Asset(id, storeId, uploadedBy, originalFilename, contentType, size, objectKey);
    }

    public void markProcessing() {
        this.status = AssetStatus.PROCESSING;
    }

    public void markReady() {
        this.status = AssetStatus.READY;
    }

    public void markFailed() {
        this.status = AssetStatus.FAILED;
    }

    public void delete() {
        this.status = AssetStatus.DELETED;
    }
}
