package io.point3.p3api.common.tenant.resolvers;

import java.util.Optional;

@FunctionalInterface
public interface TenantResolver<T> {

  Optional<String> resolveTenantIdentifier(T source);
}
