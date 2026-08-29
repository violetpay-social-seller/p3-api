package io.point3.p3api.inquiry.infrastructure.persistence;

import io.point3.p3api.inquiry.domain.entity.OrderStartReferenceAsset;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderStartReferenceAssetJpaRepository
    extends JpaRepository<OrderStartReferenceAsset, UUID> {

  @Modifying
  @Query("delete from OrderStartReferenceAsset asset where asset.inquiryId = :inquiryId")
  void deleteAllByInquiryId(@Param("inquiryId") UUID inquiryId);

  List<OrderStartReferenceAsset> findAllByInquiryIdOrderBySortOrderAsc(UUID inquiryId);
}
