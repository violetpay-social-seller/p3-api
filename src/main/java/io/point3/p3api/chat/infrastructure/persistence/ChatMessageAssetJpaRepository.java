package io.point3.p3api.chat.infrastructure.persistence;

import io.point3.p3api.chat.domain.entity.ChatMessageAsset;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageAssetJpaRepository extends JpaRepository<ChatMessageAsset, UUID> {

  List<ChatMessageAsset> findAllByMessageIdInOrderByMessageIdAscSortOrderAsc(
      Collection<UUID> messageIds);
}
