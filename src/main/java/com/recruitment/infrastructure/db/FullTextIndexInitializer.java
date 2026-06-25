package com.recruitment.infrastructure.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FullTextIndexInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_job_fts
                ON tb_job
                USING GIN (to_tsvector('simple',
                    COALESCE(title, '') || ' ' ||
                    COALESCE(description, '') || ' ' ||
                    COALESCE(requirements, '')))
                """);
            log.info("全文检索 GIN 索引已就绪");
        } catch (Exception e) {
            log.warn("创建全文索引失败: {}", e.getMessage());
        }
    }
}
