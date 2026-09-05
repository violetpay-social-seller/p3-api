package io.point3.p3api.local;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.node.TextNode;
import io.point3.p3api.inquiry.application.draft.model.OrderFormDraftData;
import io.point3.p3api.inquiry.application.result.OrderFormDraftResult;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LocalScenarioOrderFormDraftStoreTest {

  private final Clock clock = Clock.fixed(Instant.parse("2026-08-25T00:00:00Z"), ZoneOffset.UTC);
  private final LocalScenarioOrderFormDraftStore store =
      new LocalScenarioOrderFormDraftStore(clock);

  @Test
  void 드래프트는_저장_조회_삭제된다() {
    OrderFormDraftData draft = draft();

    OrderFormDraftResult result = store.save(draft);

    assertThat(result.expiresAt()).isEqualTo(Instant.parse("2026-08-25T00:30:00Z"));
    assertThat(store.findByDraftKey(result.draftKey())).contains(draft);

    store.delete(result.draftKey());

    assertThat(store.findByDraftKey(result.draftKey())).isEmpty();
  }

  private OrderFormDraftData draft() {
    UUID optionGroupId = UUID.randomUUID();
    UUID assetId = UUID.randomUUID();
    return new OrderFormDraftData(
        UUID.randomUUID(),
        UUID.randomUUID(),
        LocalDate.of(2026, 9, 1),
        LocalTime.of(14, 30),
        true,
        List.of(new OrderFormDraftData.FormAnswer(optionGroupId, TextNode.valueOf("cake"))),
        new OrderFormDraftData.ReferenceAsset(assetId, OrderFormReferenceAssetSource.STORE_GALLERY),
        true);
  }
}
