package com.recruitment.modules.job;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<JobEntity, Long> {

    Page<JobEntity> findByStatus(String status, Pageable pageable);

    @Query(value = """
        SELECT j.* FROM tb_job j WHERE j.status = 'OPEN'
        AND to_tsvector('simple', COALESCE(j.title, '') || ' ' || COALESCE(j.description, '') || ' ' || COALESCE(j.requirements, ''))
        @@ plainto_tsquery('simple', :keyword)
        ORDER BY ts_rank(to_tsvector('simple', COALESCE(j.title, '') || ' ' || COALESCE(j.description, '') || ' ' || COALESCE(j.requirements, '')),
        plainto_tsquery('simple', :keyword)) DESC
        """,
        countQuery = """
        SELECT count(*) FROM tb_job j WHERE j.status = 'OPEN'
        AND to_tsvector('simple', COALESCE(j.title, '') || ' ' || COALESCE(j.description, '') || ' ' || COALESCE(j.requirements, ''))
        @@ plainto_tsquery('simple', :keyword)
        """,
        nativeQuery = true)
    Page<JobEntity> searchOpen(@Param("keyword") String keyword, Pageable pageable);

    Page<JobEntity> findByRecruiterId(Long recruiterId, Pageable pageable);

    List<JobEntity> findByStatus(String status);
}
