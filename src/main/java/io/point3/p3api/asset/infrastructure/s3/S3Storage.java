package io.point3.p3api.asset.infrastructure.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
public class S3Storage {

    private final S3Client s3Client;

    @Value("${p3.asset.storage.original-asset-bucket}")
    private String originalBucket;

    public void store(AssetStoreRequest request) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(originalBucket)
                .contentType(request.contentType())
                .contentLength(request.sizeBytes())
                .build();

        s3Client.putObject(
                putObjectRequest, RequestBody.fromInputStream(request.inputStream(), request.sizeBytes())
        );
    }

    public void delete(String storageKey) {
        DeleteObjectRequest deleteObjectRequest =
                DeleteObjectRequest.builder().bucket(originalBucket).key(storageKey).build();

        s3Client.deleteObject(deleteObjectRequest);
    }
}
