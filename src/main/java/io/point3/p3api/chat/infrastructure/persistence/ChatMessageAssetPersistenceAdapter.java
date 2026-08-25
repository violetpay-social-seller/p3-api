package io.point3.p3api.chat.infrastructure.persistence;

import io.point3.p3api.chat.application.port.ChatMessageAssetPort;
import io.point3.p3api.chat.domain.entity.ChatMessageAsset;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatMessageAssetPersistenceAdapter implements ChatMessageAssetPort {

  private final ChatMessageAssetJpaRepository chatMessageAssetJpaRepository;

  @Override
  public List<ChatMessageAsset> saveAll(List<ChatMessageAsset> chatMessageAssets) {
    return chatMessageAssetJpaRepository.saveAll(chatMessageAssets);
  }

  @Override
  public List<ChatMessageAsset> findAllByMessageIdIn(Collection<UUID> messageIds) {
    return chatMessageAssetJpaRepository.findAllByMessageIdInOrderByMessageIdAscSortOrderAsc(
        messageIds);
  }
}
