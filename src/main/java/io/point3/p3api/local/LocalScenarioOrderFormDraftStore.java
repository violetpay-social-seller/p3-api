package io.point3.p3api.local;

import io.point3.p3api.inquiry.application.draft.model.OrderFormDraftData;
import io.point3.p3api.inquiry.application.port.OrderFormDraftStorePort;
import io.point3.p3api.inquiry.application.result.OrderFormDraftResult;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local-scenario")
public class LocalScenarioOrderFormDraftStore implements OrderFormDraftStorePort {

  private static final Duration TTL = Duration.ofMinutes(30);

  private final Map<String, DraftEntry> drafts = new ConcurrentHashMap<>();
  private final Clock clock;

  public LocalScenarioOrderFormDraftStore(Clock clock) {
    this.clock = clock;
  }

  @Override
  public OrderFormDraftResult save(OrderFormDraftData draftData) {
    String draftKey = UUID.randomUUID().toString();
    Instant expiresAt = clock.instant().plus(TTL);
    drafts.put(draftKey, new DraftEntry(draftData, expiresAt));
    return new OrderFormDraftResult(draftKey, expiresAt);
  }

  @Override
  public Optional<OrderFormDraftData> findByDraftKey(String draftKey) {
    DraftEntry entry = drafts.get(draftKey);
    if (entry == null) {
      return Optional.empty();
    }
    if (entry.expiresAt().isBefore(clock.instant())) {
      drafts.remove(draftKey);
      return Optional.empty();
    }
    return Optional.of(entry.draftData());
  }

  @Override
  public void delete(String draftKey) {
    drafts.remove(draftKey);
  }

  private record DraftEntry(OrderFormDraftData draftData, Instant expiresAt) {}
}
