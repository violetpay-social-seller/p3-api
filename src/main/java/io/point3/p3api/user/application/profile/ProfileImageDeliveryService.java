package io.point3.p3api.user.application.profile;

import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.asset.domain.type.AssetStatus;
import io.point3.p3api.assetvariant.application.AssetVariantDeliveryService;
import io.point3.p3api.assetvariant.application.result.AssetVariantDelivery;
import io.point3.p3api.user.domain.entity.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileImageDeliveryService {

  private final AssetPersistencePort assetPersistencePort;
  private final AssetVariantDeliveryService assetVariantDeliveryService;

  public String resolve(User user) {
    return resolveByUserId(List.of(user)).get(user.getId());
  }

  public Map<UUID, String> resolveByUserId(List<User> users) {
    if (users.isEmpty()) {
      return Map.of();
    }

    Map<UUID, Asset> usableAssetsById = usableAssetsById(users);
    Map<UUID, AssetVariantDelivery> deliveryByAssetId =
        assetVariantDeliveryService.resolveReadyDeliveries(
            usableAssetsById.keySet().stream().toList());

    Map<UUID, String> deliveryUrlByUserId = new HashMap<>();
    users.stream()
        .filter(user -> user.getProfileAssetId() != null)
        .forEach(user -> deliveryUrlByUserId.put(
            user.getId(),
            deliveryByAssetId
                .getOrDefault(user.getProfileAssetId(), AssetVariantDelivery.empty())
                .deliveryUrl()));
    return deliveryUrlByUserId;
  }

  private Map<UUID, Asset> usableAssetsById(List<User> users) {
    List<UUID> assetIds = users.stream()
        .map(User::getProfileAssetId)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .toList();

    return assetPersistencePort.findAllById(assetIds).stream()
        .filter(asset -> asset.getStatus() != AssetStatus.DELETED)
        .collect(Collectors.toMap(Asset::getId, asset -> asset));
  }
}
