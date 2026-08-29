package io.point3.p3api.asset.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.asset.application.port.AssetStoragePort;
import io.point3.p3api.asset.application.register.RegisterAssetCommand;
import io.point3.p3api.asset.application.result.AssetDetailResult;
import io.point3.p3api.asset.application.result.RegistryAsset;
import io.point3.p3api.asset.application.storage.StoreAssetCommand;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.asset.domain.type.AssetStatus;
import io.point3.p3api.asset.infrastructure.persistence.AssetJpaRepository;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.AssetErrorCode;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "p3.asset.delivery.base-url=https://assets.example.test")
class AssetServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private AssetService assetService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private AssetJpaRepository assetJpaRepository;

  @Autowired
  private InMemoryAssetStoragePort assetStoragePort;

  @Value("${p3.asset.delivery.base-url}")
  private String deliveryBaseUrl;

  @BeforeEach
  void setUp() {
    assetStoragePort.clear();
  }

  @Test
  @DisplayName("허용된 이미지 업로드는 외부 저장 요청을 만들고 Asset 메타데이터를 저장한다")
  void registersAssetAndStoresOriginalObject() {
    User user = saveUser();

    RegistryAsset result =
        assetService.register(command(user.getId(), "cake.png", "image/png", 1024));

    Asset persisted = assetJpaRepository.findById(result.assetId()).orElseThrow();
    assertEquals(user.getId(), persisted.getUploadedBy());
    assertEquals("cake.png", persisted.getOriginalFilename());
    assertEquals(AssetStatus.UPLOADED, persisted.getStatus());
    assertTrue(persisted.getObjectKey().endsWith("/cake.png"));
    assertEquals(deliveryBaseUrl + "/" + persisted.getObjectKey(), result.deliveryUrl());
    assertEquals(1, assetStoragePort.commands().size());
    assertEquals(persisted.getObjectKey(), assetStoragePort.commands().get(0).objectKey());
  }

  @Test
  @DisplayName("허용하지 않는 MIME 타입은 저장소 호출 전에 거절한다")
  void rejectsUnsupportedContentTypeBeforeStorageCall() {
    User user = saveUser();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> assetService.register(command(user.getId(), "cake.gif", "image/gif", 1024)));

    assertEquals(AssetErrorCode.ASSET_CONTENT_TYPE_NOT_ALLOWED, exception.getErrorCode());
    assertTrue(assetStoragePort.commands().isEmpty());
  }

  @Test
  @DisplayName("최대 크기를 초과한 파일은 저장소 호출 전에 거절한다")
  void rejectsOversizedFileBeforeStorageCall() {
    User user = saveUser();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> assetService.register(
            command(user.getId(), "large.png", "image/png", 10 * 1024 * 1024 + 1)));

    assertEquals(AssetErrorCode.ASSET_SIZE_EXCEEDED, exception.getErrorCode());
    assertTrue(assetStoragePort.commands().isEmpty());
  }

  @Test
  @DisplayName("Asset 상세 조회는 업로드 사용자 범위를 벗어나면 찾을 수 없다")
  void getsOnlyOwnedAsset() {
    User owner = saveUser();
    User anotherUser = saveUser();
    RegistryAsset registered =
        assetService.register(command(owner.getId(), "cake.webp", "image/webp", 1024));

    AssetDetailResult detail = assetService.getAsset(registered.assetId(), owner.getId());
    BaseException exception = assertThrows(
        BaseException.class,
        () -> assetService.getAsset(registered.assetId(), anotherUser.getId()));

    assertEquals(registered.assetId(), detail.id());
    assertFalse(detail.deliveryUrl().isBlank());
    assertEquals(AssetErrorCode.ASSET_NOT_FOUND, exception.getErrorCode());
  }

  private User saveUser() {
    return userJpaRepository.saveAndFlush(User.create(
        UUID.randomUUID().toString(),
        uniqueEmail("asset-user"),
        "사용자",
        UserRole.BUYER,
        "010-0000-0000",
        SignupProvider.GOOGLE));
  }

  private RegisterAssetCommand command(
      UUID uploadedBy, String originalFilename, String contentType, long sizeBytes) {
    return new RegisterAssetCommand(
        uploadedBy,
        new ByteArrayInputStream("image".getBytes(StandardCharsets.UTF_8)),
        originalFilename,
        contentType,
        sizeBytes);
  }

  @TestConfiguration
  static class AssetServiceTestConfiguration {

    @Bean
    @Primary
    InMemoryAssetStoragePort inMemoryAssetStoragePort() {
      return new InMemoryAssetStoragePort();
    }
  }

  static class InMemoryAssetStoragePort implements AssetStoragePort {
    private final List<StoreAssetCommand> commands = new ArrayList<>();

    @Override
    public void store(StoreAssetCommand command) {
      commands.add(command);
    }

    List<StoreAssetCommand> commands() {
      return List.copyOf(commands);
    }

    void clear() {
      commands.clear();
    }
  }
}
