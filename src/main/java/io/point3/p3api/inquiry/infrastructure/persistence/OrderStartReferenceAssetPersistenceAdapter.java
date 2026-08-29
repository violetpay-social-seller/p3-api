package io.point3.p3api.inquiry.infrastructure.persistence;

import io.point3.p3api.inquiry.application.port.OrderStartReferenceAssetPersistencePort;
import io.point3.p3api.inquiry.domain.entity.OrderStartReferenceAsset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class OrderStartReferenceAssetPersistenceAdapter
    implements OrderStartReferenceAssetPersistencePort {

  private final OrderStartReferenceAssetJpaRepository orderStartReferenceAssetJpaRepository;

  @Override
  public List<OrderStartReferenceAsset> saveAll(List<OrderStartReferenceAsset> assets) {
    return orderStartReferenceAssetJpaRepository.saveAll(assets);
  }

  @Override
  public void deleteAllByInquiryId(UUID inquiryId) {
    orderStartReferenceAssetJpaRepository.deleteAllByInquiryId(inquiryId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderStartReferenceAsset> findAllByInquiryId(UUID inquiryId) {
    return orderStartReferenceAssetJpaRepository.findAllByInquiryIdOrderBySortOrderAsc(inquiryId);
  }
}
