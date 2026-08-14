package io.point3.p3api.chat.application.send;

public interface SendChatMessageUseCase {

  SendChatMessageResult execute(SendChatMessageCommand command);
}
