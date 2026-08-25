package io.point3.p3api.orderform.application.port;

import io.point3.p3api.orderform.domain.entity.OrderFormField;
import io.point3.p3api.orderform.domain.entity.OrderFormFieldGroup;
import io.point3.p3api.orderform.domain.entity.OrderFormFieldOption;
import io.point3.p3api.orderform.domain.entity.OrderFormTemplate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderFormPersistencePort {

  OrderFormTemplate saveTemplate(OrderFormTemplate template);

  OrderFormFieldGroup saveGroup(OrderFormFieldGroup group);

  List<OrderFormField> saveFields(List<OrderFormField> fields);

  List<OrderFormFieldOption> saveOptions(List<OrderFormFieldOption> options);

  Optional<OrderFormTemplate> findTemplateByIdAndStoreId(UUID templateId, UUID storeId);

  Optional<OrderFormTemplate> findActiveTemplateByStoreId(UUID storeId);

  boolean existsActiveTemplateByStoreId(UUID storeId);

  List<OrderFormField> findFieldsByTemplateId(UUID templateId);

  List<OrderFormFieldGroup> findGroupsByTemplateId(UUID templateId);

  List<OrderFormFieldOption> findOptionsByFieldIds(List<UUID> fieldIds);

  void deleteGroupsByTemplateId(UUID templateId);
}
