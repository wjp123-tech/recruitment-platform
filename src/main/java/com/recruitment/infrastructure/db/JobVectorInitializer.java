package com.recruitment.infrastructure.db;

import com.recruitment.modules.job.JobEntity;
import com.recruitment.modules.job.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobVectorInitializer implements ApplicationRunner {

    private final JobRepository jobRepository;
    private final VectorStore vectorStore;

    @Override
    public void run(ApplicationArguments args) {
        List<JobEntity> jobs = jobRepository.findByStatus("OPEN");
        int count = 0;
        for (JobEntity job : jobs) {
            try {
                String text = job.getTitle() + " " +
                    (job.getDescription() != null ? job.getDescription() : "") + " " +
                    (job.getRequirements() != null ? job.getRequirements() : "");
                Document doc = new Document(text);
                doc.getMetadata().put("job_id", job.getId().toString());
                vectorStore.add(List.of(doc));
                count++;
            } catch (Exception e) {
                log.warn("岗位向量化失败 jobId={}: {}", job.getId(), e.getMessage());
            }
        }
        if (count > 0) {
            log.info("已向量化 {} 个岗位", count);
        }
    }
}
