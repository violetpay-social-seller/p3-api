package io.point3.p3api.orderform.infrastructure.persistence;

import io.point3.p3api.orderform.application.port.OrderFormPersistencePort;
import io.point3.p3api.orderform.domain.entity.OrderFormField;
import io.point3.p3api.orderform.domain.entity.OrderFormFieldGroup;
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
  private final OrderFormFieldGroupJpaRepository orderFormFieldGroupJpaRepository;
  private final OrderFormFieldJpaRepository orderFormFieldJpaRepository;

  @Override
  public OrderFormTemplate saveTemplate(OrderFormTemplate template) {
    return orderFormTemplateJpaRepository.save(template);
  }

  @Override
  public OrderFormFieldGroup saveGroup(OrderFormFieldGroup group) {
    return orderFormFieldGroupJpaRepository.save(group);
  }

  @Override
  public List<OrderFormField> saveFields(List<OrderFormField> fields) {
    return orderFormFieldJpaRepository.saveAll(fields);
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
  public List<OrderFormField> findFieldsByTemplateId(UUID templateId) {
    List<UUID> groupIds =
        orderFormFieldGroupJpaRepository.findAllByTemplateIdOrderBySortOrderAsc(templateId).stream()
            .map(OrderFormFieldGroup::getId)
            .toList();
    return orderFormFieldJpaRepository.findAllByGroupIdInOrderBySortOrderAsc(groupIds);
  }

  @Override
  public void deleteGroupsByTemplateId(UUID templateId) {
    orderFormFieldGroupJpaRepository.deleteByTemplateId(templateId);
    orderFormFieldGroupJpaRepository.flush();
  }
}
