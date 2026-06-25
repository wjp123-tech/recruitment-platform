package com.recruitment.modules.delivery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<DeliveryEntity, Long> {
    Page<DeliveryEntity> findByJobSeekerId(Long jobSeekerId, Pageable pageable);
    Page<DeliveryEntity> findByJobId(Long jobId, Pageable pageable);
    Optional<DeliveryEntity> findByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);
}
