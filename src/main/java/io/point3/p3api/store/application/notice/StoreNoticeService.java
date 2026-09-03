package io.point3.p3api.store.application.notice;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.application.notice.command.UpdateStoreNoticesCommand;
import io.point3.p3api.store.application.notice.port.StoreNoticePersistencePort;
import io.point3.p3api.store.application.notice.query.StoreNoticeQueryUseCase;
import io.point3.p3api.store.application.notice.result.StoreNoticeResult;
import io.point3.p3api.store.application.notice.update.StoreNoticeUpdateUseCase;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.domain.entity.StoreNotice;
import io.point3.p3api.store.domain.type.StoreNoticeType;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StoreNoticeService implements StoreNoticeQueryUseCase, StoreNoticeUpdateUseCase {

  private final StorePersistencePort storePersistencePort;
  private final StoreNoticePersistencePort storeNoticePersistencePort;

  @Override
  @Transactional(readOnly = true)
  public StoreNoticeResult getNotices(UUID storeId) {
    requireStore(storeId);
    return StoreNoticeResult.from(storeNoticePersistencePort.findAllByStoreId(storeId));
  }

  @Override
  public StoreNoticeResult update(UpdateStoreNoticesCommand command) {
    validate(command);
    requireStore(command.storeId());
    List<StoreNotice> saved = storeNoticePersistencePort.replaceAllByStoreId(
        command.storeId(),
        command.notices().stream()
            .flatMap(notice -> java.util.stream.IntStream.range(
                    0, notice.items().size())
                .mapToObj(index -> StoreNotice.create(
                    command.storeId(), notice.type(), notice.items().get(index).content(), index)))
            .toList());
    return StoreNoticeResult.from(saved);
  }

  private void validate(UpdateStoreNoticesCommand command) {
    if (command == null || command.storeId() == null || command.notices() == null) {
      throw invalid();
    }
    if (command.notices().size() != StoreNoticeType.values().length) {
      throw invalid();
    }
    EnumSet<StoreNoticeType> types = EnumSet.noneOf(StoreNoticeType.class);
    for (UpdateStoreNoticesCommand.Notice notice : command.notices()) {
      if (notice == null
          || notice.type() == null
          || !types.add(notice.type())
          || notice.items() == null) {
        throw invalid();
      }
      for (UpdateStoreNoticesCommand.Item item : notice.items()) {
        if (item == null || item.content() == null || item.content().isBlank()) {
          throw invalid();
        }
      }
    }
    if (!types.equals(EnumSet.allOf(StoreNoticeType.class))) {
      throw invalid();
    }
  }

  private void requireStore(UUID storeId) {
    storePersistencePort
        .findById(storeId)
        .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));
  }

  private BaseException invalid() {
    return new BaseException(CommonErrorCode.INVALID_INPUT);
  }
}
