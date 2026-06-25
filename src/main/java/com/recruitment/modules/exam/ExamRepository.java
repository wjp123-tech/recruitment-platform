package com.recruitment.modules.exam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<ExamEntity, Long> {
    List<ExamEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
