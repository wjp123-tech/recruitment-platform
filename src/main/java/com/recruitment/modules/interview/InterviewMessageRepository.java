package com.recruitment.modules.interview;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewMessageRepository extends JpaRepository<InterviewMessageEntity, Long> {
    List<InterviewMessageEntity> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
