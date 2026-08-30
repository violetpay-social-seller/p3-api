package io.point3.p3api.orderform.infrastructure.persistence;

import io.point3.p3api.orderform.application.port.OrderFormPersistencePort;
import io.point3.p3api.orderform.domain.entity.OrderFormCategoryGroup;
import io.point3.p3api.orderform.domain.entity.OrderFormOption;
import io.point3.p3api.orderform.domain.entity.OrderFormOptionGroup;
import io.point3.p3api.orderform.domain.entity.OrderFormTemplate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class OrderFormPersistenceAdapter implements OrderFormPersistencePort {

  private final OrderFormTemplateJpaRepository orderFormTemplateJpaRepository;
  private final OrderFormCategoryGroupJpaRepository orderFormCategoryGroupJpaRepository;
  private final OrderFormOptionGroupJpaRepository orderFormOptionGroupJpaRepository;
  private final OrderFormOptionJpaRepository orderFormOptionJpaRepository;

  @Override
  public OrderFormTemplate saveTemplate(OrderFormTemplate template) {
    return orderFormTemplateJpaRepository.save(template);
  }

  @Override
  public OrderFormCategoryGroup saveCategoryGroup(OrderFormCategoryGroup group) {
    return orderFormCategoryGroupJpaRepository.save(group);
  }

  @Override
  public List<OrderFormOptionGroup> saveOptionGroups(List<OrderFormOptionGroup> optionGroups) {
    return orderFormOptionGroupJpaRepository.saveAll(optionGroups);
  }

  @Override
  public List<OrderFormOption> saveOptions(List<OrderFormOption> options) {
    return orderFormOptionJpaRepository.saveAll(options);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<OrderFormTemplate> findTemplateByIdAndStoreId(UUID templateId, UUID storeId) {
    return orderFormTemplateJpaRepository.findByIdAndStoreId(templateId, storeId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<OrderFormTemplate> findActiveTemplateByStoreId(UUID storeId) {
    return orderFormTemplateJpaRepository.findByStoreIdAndActiveTrue(storeId);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsActiveTemplateByStoreId(UUID storeId) {
    return orderFormTemplateJpaRepository.existsByStoreIdAndActiveTrue(storeId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderFormOptionGroup> findOptionGroupsByTemplateId(UUID templateId) {
    List<UUID> groupIds =
        orderFormCategoryGroupJpaRepository
            .findAllByTemplateIdOrderBySortOrderAsc(templateId)
            .stream()
            .map(OrderFormCategoryGroup::getId)
            .toList();
    return orderFormOptionGroupJpaRepository.findAllByCategoryGroupIdInOrderBySortOrderAsc(
        groupIds);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderFormCategoryGroup> findCategoryGroupsByTemplateId(UUID templateId) {
    return orderFormCategoryGroupJpaRepository.findAllByTemplateIdOrderBySortOrderAsc(templateId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderFormOption> findOptionsByOptionGroupIds(List<UUID> optionGroupIds) {
    if (optionGroupIds.isEmpty()) {
      return List.of();
    }
    return orderFormOptionJpaRepository.findAllByOptionGroupIdInOrderBySortOrderAsc(optionGroupIds);
  }

  @Override
  public void deleteCategoryGroupsByTemplateId(UUID templateId) {
    orderFormCategoryGroupJpaRepository.deleteByTemplateId(templateId);
    orderFormCategoryGroupJpaRepository.flush();
  }
}
