package com.recruitment.modules.resume;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<ResumeEntity, Long> {
    List<ResumeEntity> findByUserId(Long userId);
}
