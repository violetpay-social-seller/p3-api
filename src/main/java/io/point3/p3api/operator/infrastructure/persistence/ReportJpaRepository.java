package io.point3.p3api.operator.infrastructure.persistence;

import io.point3.p3api.operator.domain.entity.Report;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReportJpaRepository
    extends JpaRepository<Report, UUID>, JpaSpecificationExecutor<Report> {}
