package io.point3.p3api.chat.application.send;

import io.point3.p3api.chat.application.port.ChatMessagePort;
import io.point3.p3api.chat.application.port.ChatParticipantPort;
import io.point3.p3api.chat.domain.entity.ChatMessage;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.ChatErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SendChatMessage {

  private final ChatParticipantPort chatParticipantPort;
  private final ChatMessagePort chatMessagePort;

  @Transactional
  public ChatMessage execute(SendChatMessageCommand command) {
    if (!chatParticipantPort.existsByChatRoomIdAndUserId(
        command.chatRoomId(), command.senderId())) {
      throw new BaseException(ChatErrorCode.CHAT_PARTICIPANT_FORBIDDEN);
    }

    ChatMessage chatMessage =
        ChatMessage.create(command.chatRoomId(), command.senderId(), command.content());

    return chatMessagePort.save(chatMessage);
  }
}
