package io.point3.p3api.chat.application.port;

import io.point3.p3api.chat.domain.entity.ChatEvent;

public interface ChatEventPort {

  ChatEvent save(ChatEvent chatEvent);
}
