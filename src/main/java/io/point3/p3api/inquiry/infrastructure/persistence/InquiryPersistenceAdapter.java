package io.point3.p3api.inquiry.infrastructure.persistence;

import io.point3.p3api.inquiry.application.port.InquiryPersistencePort;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
@RequiredArgsConstructor
public class InquiryPersistenceAdapter implements InquiryPersistencePort {

    private final InquiryJpaRepository inquiryJpaRepository;

    @Override
    public Inquiry save(Inquiry inquiry) {
        return inquiryJpaRepository.save(inquiry);
    }

    @Override
    public Optional<Inquiry> findByStoreIdAndBuyerUserId(UUID storeId, UUID userId) {
        return inquiryJpaRepository.findByStoreIdAndBuyerUserId(storeId, userId);
    }
}
