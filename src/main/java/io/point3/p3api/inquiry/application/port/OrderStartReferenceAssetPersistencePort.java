package io.point3.p3api.inquiry.application.port;

import io.point3.p3api.inquiry.domain.entity.OrderStartReferenceAsset;
import java.util.List;
import java.util.UUID;

public interface OrderStartReferenceAssetPersistencePort {

  List<OrderStartReferenceAsset> saveAll(List<OrderStartReferenceAsset> assets);

  void deleteAllByInquiryId(UUID inquiryId);

  List<OrderStartReferenceAsset> findAllByInquiryId(UUID inquiryId);
}
