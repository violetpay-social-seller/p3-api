package io.point3.p3api.store.notice.application;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.notice.application.command.UpdateStoreNoticesCommand;
import io.point3.p3api.store.notice.application.port.StoreNoticePersistencePort;
import io.point3.p3api.store.notice.application.query.StoreNoticeQueryUseCase;
import io.point3.p3api.store.notice.application.result.StoreNoticeResult;
import io.point3.p3api.store.notice.application.update.StoreNoticeUpdateUseCase;
import io.point3.p3api.store.notice.domain.entity.StoreNotice;
import io.point3.p3api.store.notice.domain.type.StoreNoticeType;
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
            .filter(notice -> notice.content() != null)
            .map(notice -> StoreNotice.create(command.storeId(), notice.type(), notice.content()))
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
          || (notice.content() != null && notice.content().isBlank())) {
        throw invalid();
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
