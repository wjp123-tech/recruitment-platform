package com.recruitment.modules.interview;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterviewReportRepository extends JpaRepository<InterviewReportEntity, Long> {
    Optional<InterviewReportEntity> findBySessionId(Long sessionId);
}
