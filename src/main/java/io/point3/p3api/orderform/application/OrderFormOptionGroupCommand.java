package io.point3.p3api.orderform.application;

import io.point3.p3api.orderform.domain.type.SelectionType;
import java.util.List;

public interface OrderFormOptionGroupCommand {

  String label();

  SelectionType selectionType();

  boolean required();

  int sortOrder();

  List<OrderFormOptionCommand> options();
}
