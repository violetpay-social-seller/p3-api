package io.point3.p3api.chat.infrastructure.persistence;

import io.point3.p3api.chat.domain.entity.ChatEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatEventJpaRepository extends JpaRepository<ChatEvent, UUID> {}
