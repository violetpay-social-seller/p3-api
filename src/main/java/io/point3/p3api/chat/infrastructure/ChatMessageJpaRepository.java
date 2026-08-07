package io.point3.p3api.chat.infrastructure;

import io.point3.p3api.chat.domain.entity.ChatMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageJpaRepository extends JpaRepository<ChatMessage, UUID> {}
