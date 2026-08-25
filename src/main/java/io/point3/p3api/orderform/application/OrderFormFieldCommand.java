package io.point3.p3api.orderform.application;

import io.point3.p3api.orderform.domain.type.FieldType;
import java.util.List;

public interface OrderFormFieldCommand {

  String label();

  FieldType fieldType();

  boolean required();

  String settings();

  int sortOrder();

  List<OrderFormFieldOptionCommand> options();
}
