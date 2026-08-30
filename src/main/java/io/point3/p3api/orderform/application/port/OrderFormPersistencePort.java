package io.point3.p3api.orderform.application.port;

import io.point3.p3api.orderform.domain.entity.OrderFormCategoryGroup;
import io.point3.p3api.orderform.domain.entity.OrderFormOption;
import io.point3.p3api.orderform.domain.entity.OrderFormOptionGroup;
import io.point3.p3api.orderform.domain.entity.OrderFormTemplate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderFormPersistencePort {

  OrderFormTemplate saveTemplate(OrderFormTemplate template);

  OrderFormCategoryGroup saveCategoryGroup(OrderFormCategoryGroup group);

  List<OrderFormOptionGroup> saveOptionGroups(List<OrderFormOptionGroup> optionGroups);

  List<OrderFormOption> saveOptions(List<OrderFormOption> options);

  Optional<OrderFormTemplate> findTemplateByIdAndStoreId(UUID templateId, UUID storeId);

  Optional<OrderFormTemplate> findActiveTemplateByStoreId(UUID storeId);

  boolean existsActiveTemplateByStoreId(UUID storeId);

  List<OrderFormOptionGroup> findOptionGroupsByTemplateId(UUID templateId);

  List<OrderFormCategoryGroup> findCategoryGroupsByTemplateId(UUID templateId);

  List<OrderFormOption> findOptionsByOptionGroupIds(List<UUID> optionGroupIds);

  void deleteCategoryGroupsByTemplateId(UUID templateId);
}
