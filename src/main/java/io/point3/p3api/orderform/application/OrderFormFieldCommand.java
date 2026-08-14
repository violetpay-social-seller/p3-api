package io.point3.p3api.orderform.application;

import io.point3.p3api.orderform.domain.type.FieldType;

public interface OrderFormFieldCommand {

  String label();

  FieldType fieldType();

  boolean required();

  String settings();

  int sortOrder();
}
