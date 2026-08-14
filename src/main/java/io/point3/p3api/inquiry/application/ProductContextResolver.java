package io.point3.p3api.inquiry.application;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.ProductErrorCode;
import io.point3.p3api.product.application.port.ProductPersistencePort;
import io.point3.p3api.product.domain.entity.Product;
import io.point3.p3api.product.domain.type.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductContextResolver {

    private final ProductPersistencePort productPersistencePort;

    // 구매자가 주문서에서 제출한 상품의 유효성 검증
    public UUID resolve(UUID storeId, UUID productId) {
        if (productId == null) {
            return null;
        }

        Product product = productPersistencePort
                .findByIdAndStoreIdAndStatus(productId, storeId, ProductStatus.VISIBLE)
                .orElseThrow(() -> new BaseException(ProductErrorCode.PRODUCT_NOT_FOUND));

        return product.getId();
    }
}
