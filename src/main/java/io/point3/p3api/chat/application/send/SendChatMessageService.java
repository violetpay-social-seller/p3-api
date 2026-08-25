package io.point3.p3api.chat.application.send;

import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.chat.application.port.ChatMessageAssetPort;
import io.point3.p3api.chat.application.port.ChatMessagePort;
import io.point3.p3api.chat.application.timeline.ChatTimelineItemPublisher;
import io.point3.p3api.chat.domain.entity.ChatMessage;
import io.point3.p3api.chat.domain.entity.ChatMessageAsset;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.AssetErrorCode;
import io.point3.p3api.exception.code.CommonErrorCode;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SendChatMessageService implements SendChatMessageUseCase {
  private static final int MAX_ASSET_COUNT = 5;

  private final ChatMessagePort chatMessagePort;
  private final ChatMessageAssetPort chatMessageAssetPort;
  private final AssetPersistencePort assetPersistencePort;
  private final ChatTimelineItemPublisher chatTimelineItemPublisher;

  @Override
  @Transactional
  public SendChatMessageResult execute(SendChatMessageCommand command) {
    validate(command);

    ChatMessage chatMessage =
        ChatMessage.create(command.inquiryId(), command.senderUserId(), command.content());

    ChatMessage savedChatMessage = chatMessagePort.save(chatMessage);
    List<ChatMessageAsset> savedAssets = chatMessageAssetPort.saveAll(
        createMessageAssets(savedChatMessage.getId(), command.assetIds()));

    ChatTimelineItem savedChatTimelineItem = chatTimelineItemPublisher.publishMessage(
        savedChatMessage.getInquiryId(),
        savedChatMessage.getSenderUserId(),
        savedChatMessage.getId());

    return new SendChatMessageResult(savedChatMessage, savedChatTimelineItem, savedAssets);
  }

  private void validate(SendChatMessageCommand command) {
    boolean hasContent = command.content() != null && !command.content().isBlank();
    boolean hasAssets = !command.assetIds().isEmpty();

    if (!hasContent && !hasAssets) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }

    if (command.content() != null && command.content().isBlank()) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }

    if (command.assetIds().size() > MAX_ASSET_COUNT
        || command.assetIds().stream().anyMatch(Objects::isNull)
        || hasDuplicateAssets(command.assetIds())) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }

    command.assetIds().forEach(assetId -> ensureSenderAsset(assetId, command.senderUserId()));
  }

  private boolean hasDuplicateAssets(List<UUID> assetIds) {
    Set<UUID> uniqueAssetIds = new HashSet<>(assetIds);
    return uniqueAssetIds.size() != assetIds.size();
  }

  private void ensureSenderAsset(UUID assetId, UUID senderUserId) {
    assetPersistencePort
        .findByIdAndUploadedBy(assetId, senderUserId)
        .orElseThrow(() -> new BaseException(AssetErrorCode.ASSET_NOT_FOUND));
  }

  private List<ChatMessageAsset> createMessageAssets(UUID messageId, List<UUID> assetIds) {
    return IntStream.range(0, assetIds.size())
        .mapToObj(index -> ChatMessageAsset.create(messageId, assetIds.get(index), index))
        .toList();
  }
}
