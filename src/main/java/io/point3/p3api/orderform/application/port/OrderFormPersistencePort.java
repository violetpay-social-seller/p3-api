package io.point3.p3api.orderform.application.port;

import io.point3.p3api.orderform.domain.entity.OrderFormField;
import io.point3.p3api.orderform.domain.entity.OrderFormTemplate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderFormPersistencePort {

  OrderFormTemplate saveTemplate(OrderFormTemplate template);

  List<OrderFormField> saveFields(List<OrderFormField> fields);

  Optional<OrderFormTemplate> findTemplateByIdAndStoreId(UUID templateId, UUID storeId);

  Optional<OrderFormTemplate> findActiveTemplateByStoreId(UUID storeId);

  boolean existsActiveTemplateByStoreId(UUID storeId);

  List<OrderFormField> findFieldsByTemplateId(UUID templateId);

  void deleteFieldsByTemplateId(UUID templateId);
}
