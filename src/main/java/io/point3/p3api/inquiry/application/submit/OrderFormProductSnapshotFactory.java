package io.point3.p3api.inquiry.application.submit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.exception.code.ProductErrorCode;
import io.point3.p3api.inquiry.application.command.SubmitPreOrderCommand;
import io.point3.p3api.product.application.port.ProductPersistencePort;
import io.point3.p3api.product.domain.entity.Product;
import io.point3.p3api.product.domain.entity.ProductOption;
import io.point3.p3api.product.domain.entity.ProductOptionGroup;
import io.point3.p3api.product.domain.type.ProductStatus;
import io.point3.p3api.product.domain.type.SelectionType;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 상품 기반 주문서 제출에서 상품과 선택 옵션의 저장용 스냅샷을 만든다. */
@Component
@RequiredArgsConstructor
public class OrderFormProductSnapshotFactory {

  private final ProductPersistencePort productPersistencePort;
  private final ObjectMapper objectMapper;

  public ProductSubmissionSnapshot create(
      UUID storeId, UUID productId, List<SubmitPreOrderCommand.ProductOptionSelection> selections) {
    if (productId == null) {
      if (!selections.isEmpty()) {
        throw new BaseException(ProductErrorCode.PRODUCT_OPTION_INVALID);
      }

      return ProductSubmissionSnapshot.empty();
    }

    Product product = productPersistencePort
        .findByIdAndStoreIdAndStatus(productId, storeId, ProductStatus.VISIBLE)
        .orElseThrow(() -> new BaseException(ProductErrorCode.PRODUCT_NOT_FOUND));

    return new ProductSubmissionSnapshot(
        product.getId(),
        writeProductSnapshot(product),
        writeProductOptionSnapshot(product, selections));
  }

  private String writeProductSnapshot(Product product) {
    ProductSnapshot snapshot = new ProductSnapshot(
        product.getId(), product.getName(), product.getDescription(), product.getBasePrice());
    return write(snapshot);
  }

  private String writeProductOptionSnapshot(
      Product product, List<SubmitPreOrderCommand.ProductOptionSelection> selections) {
    List<ProductOptionGroup> groups =
        productPersistencePort.findOptionGroupsByProductId(product.getId());
    Map<UUID, ProductOptionGroup> groupMap =
        groups.stream().collect(Collectors.toMap(ProductOptionGroup::getId, Function.identity()));

    List<UUID> groupIds = groups.stream().map(ProductOptionGroup::getId).toList();
    Map<UUID, ProductOption> optionMap =
        productPersistencePort.findActiveOptionsByOptionGroupIds(groupIds).stream()
            .collect(Collectors.toMap(ProductOption::getId, Function.identity()));

    Map<UUID, SubmitPreOrderCommand.ProductOptionSelection> selectionMap = selections.stream()
        .collect(Collectors.toMap(
            SubmitPreOrderCommand.ProductOptionSelection::optionGroupId, Function.identity()));

    validateRequiredGroups(groups, selectionMap);

    List<ProductOptionGroupSnapshot> snapshots = selections.stream()
        .map(selection -> toGroupSnapshot(selection, groupMap, optionMap))
        .sorted(Comparator.comparingInt(ProductOptionGroupSnapshot::sortOrder))
        .toList();

    return write(snapshots);
  }

  private void validateRequiredGroups(
      List<ProductOptionGroup> groups,
      Map<UUID, SubmitPreOrderCommand.ProductOptionSelection> selectionMap) {
    boolean missingRequiredGroup = groups.stream()
        .anyMatch(group -> group.isRequired() && !selectionMap.containsKey(group.getId()));

    if (missingRequiredGroup) {
      throw new BaseException(ProductErrorCode.PRODUCT_OPTION_INVALID);
    }
  }

  private ProductOptionGroupSnapshot toGroupSnapshot(
      SubmitPreOrderCommand.ProductOptionSelection selection,
      Map<UUID, ProductOptionGroup> groupMap,
      Map<UUID, ProductOption> optionMap) {
    ProductOptionGroup group = groupMap.get(selection.optionGroupId());
    if (group == null) {
      throw new BaseException(ProductErrorCode.PRODUCT_OPTION_INVALID);
    }

    List<ProductOption> selectedOptions = selection.optionIds().stream()
        .map(optionMap::get)
        .peek(option -> {
          if (option == null || !option.getOptionGroupId().equals(group.getId())) {
            throw new BaseException(ProductErrorCode.PRODUCT_OPTION_INVALID);
          }
        })
        .sorted(Comparator.comparingInt(ProductOption::getSortOrder))
        .toList();

    validateSelectionType(group, selectedOptions);

    List<ProductOptionSnapshot> optionSnapshots = selectedOptions.stream()
        .map(option -> new ProductOptionSnapshot(
            option.getId(), option.getName(), option.getAdditionalPrice(), option.getSortOrder()))
        .toList();

    return new ProductOptionGroupSnapshot(
        group.getId(),
        group.getName(),
        group.getSelectionType().name(),
        group.isRequired(),
        group.getSortOrder(),
        optionSnapshots);
  }

  private void validateSelectionType(
      ProductOptionGroup group, List<ProductOption> selectedOptions) {
    if (group.getSelectionType() == SelectionType.SINGLE && selectedOptions.size() != 1) {
      throw new BaseException(ProductErrorCode.PRODUCT_OPTION_INVALID);
    }

    if (selectedOptions.isEmpty()) {
      throw new BaseException(ProductErrorCode.PRODUCT_OPTION_INVALID);
    }
  }

  private String write(Object snapshot) {
    try {
      return objectMapper.writeValueAsString(snapshot);
    } catch (JsonProcessingException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  public record ProductSubmissionSnapshot(
      UUID productId, String productSnapshot, String productOptionSnapshot) {

    public static ProductSubmissionSnapshot empty() {
      return new ProductSubmissionSnapshot(null, null, null);
    }
  }

  private record ProductSnapshot(UUID productId, String name, String description, Long basePrice) {}

  private record ProductOptionGroupSnapshot(
      UUID optionGroupId,
      String name,
      String selectionType,
      boolean required,
      int sortOrder,
      List<ProductOptionSnapshot> options) {}

  private record ProductOptionSnapshot(
      UUID optionId, String name, long additionalPrice, int sortOrder) {}
}
