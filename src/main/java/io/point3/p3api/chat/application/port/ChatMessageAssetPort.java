package io.point3.p3api.chat.application.port;

import io.point3.p3api.chat.domain.entity.ChatMessageAsset;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ChatMessageAssetPort {

  List<ChatMessageAsset> saveAll(List<ChatMessageAsset> chatMessageAssets);

  List<ChatMessageAsset> findAllByMessageIdIn(Collection<UUID> messageIds);
}
