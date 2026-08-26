package io.point3.p3api.operator.infrastructure.persistence;

import io.point3.p3api.operator.domain.entity.OperatorActionLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OperatorActionLogJpaRepository
    extends JpaRepository<OperatorActionLog, UUID>, JpaSpecificationExecutor<OperatorActionLog> {}
