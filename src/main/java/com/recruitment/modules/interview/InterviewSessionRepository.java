package com.recruitment.modules.interview;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewSessionRepository extends JpaRepository<InterviewSessionEntity, Long> {
    List<InterviewSessionEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
