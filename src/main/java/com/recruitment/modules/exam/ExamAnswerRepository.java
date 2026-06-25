package com.recruitment.modules.exam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamAnswerRepository extends JpaRepository<ExamAnswerEntity, Long> {
    List<ExamAnswerEntity> findByExamId(Long examId);
}
