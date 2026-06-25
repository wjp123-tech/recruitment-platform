package com.recruitment.modules.job;

import com.recruitment.common.exception.BusinessException;
import com.recruitment.common.exception.ErrorCode;
import com.recruitment.modules.user.UserEntity;
import com.recruitment.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final VectorStore vectorStore;

    public record JobCreateReq(String title, String description, String requirements,
                                String salaryRange, String location, String jobType) {}
    public record JobListResp(Long id, String title, String location, String salaryRange,
                               String jobType, String status, LocalDateTime createdAt) {}

    public JobEntity create(Long recruiterId, JobCreateReq req) {
        JobEntity job = JobEntity.builder()
            .recruiterId(recruiterId)
            .title(req.title())
            .description(req.description())
            .requirements(req.requirements())
            .salaryRange(req.salaryRange())
            .location(req.location())
            .jobType(req.jobType())
            .build();
        job = jobRepository.save(job);
        // 异步向量化岗位描述（VectorStore 内部自动调用 Embedding）
        try {
            String text = req.title() + " " + req.description() + " " + req.requirements();
            Document doc = new Document(text);
            doc.getMetadata().put("job_id", job.getId().toString());
            vectorStore.add(List.of(doc));
        } catch (Exception e) {
            log.warn("岗位向量化失败: jobId={}, error={}", job.getId(), e.getMessage());
        }
        return job;
    }

    public JobEntity update(Long recruiterId, Long jobId, JobCreateReq req) {
        JobEntity job = jobRepository.findById(jobId)
            .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));
        if (!job.getRecruiterId().equals(recruiterId)) {
            throw new BusinessException(ErrorCode.JOB_PERMISSION_DENIED);
        }
        job.setTitle(req.title());
        job.setDescription(req.description());
        job.setRequirements(req.requirements());
        job.setSalaryRange(req.salaryRange());
        job.setLocation(req.location());
        job.setJobType(req.jobType());
        return jobRepository.save(job);
    }

    public void delete(Long recruiterId, Long jobId) {
        JobEntity job = jobRepository.findById(jobId)
            .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));
        if (!job.getRecruiterId().equals(recruiterId)) {
            throw new BusinessException(ErrorCode.JOB_PERMISSION_DENIED);
        }
        jobRepository.delete(job);
    }

    public JobEntity getById(Long jobId) {
        return jobRepository.findById(jobId)
            .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));
    }

    public Page<JobEntity> listOpen(String keyword, int page, int size) {
        if (keyword != null && !keyword.isBlank()) {
            return jobRepository.searchOpen(keyword, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        }
        return jobRepository.findByStatus("OPEN", PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public Page<JobEntity> myJobs(Long recruiterId, int page, int size) {
        return jobRepository.findByRecruiterId(recruiterId,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    /**
     * AI 岗位推荐：基于求职者意向 与 岗位向量做语义匹配
     */
    public List<JobEntity> recommend(Long userId, int topK) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return jobRepository.findByStatus("OPEN", PageRequest.of(0, topK)).getContent();
        }

        // 构建求职意向文本
        String preference = buildPreferenceText(user);
        if (preference.isBlank()) {
            // 未设置意向，返回最新岗位
            return jobRepository.findByStatus("OPEN", PageRequest.of(0, topK)).getContent();
        }

        List<JobEntity> aiResults = List.of();
        try {
            List<Document> results = vectorStore.similaritySearch(
                org.springframework.ai.vectorstore.SearchRequest.builder()
                    .query(preference)
                    .topK(topK * 2)
                    .similarityThreshold(0.25)
                    .build()
            );
            aiResults = results.stream()
                .map(d -> {
                    String jobId = (String) d.getMetadata().get("job_id");
                    return jobId != null ? jobRepository.findById(Long.parseLong(jobId)).orElse(null) : null;
                })
                .filter(j -> j != null && "OPEN".equals(j.getStatus()))
                .limit(topK)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("AI向量检索失败: {}", e.getMessage());
        }

        // AI 结果为空 → fallback 到全文检索
        if (aiResults.isEmpty() && user.getDesiredTitle() != null && !user.getDesiredTitle().isBlank()) {
            aiResults = jobRepository.searchOpen(user.getDesiredTitle(),
                PageRequest.of(0, topK)).getContent();
        }
        // 全文检索也没有 → 返回最新岗位
        if (aiResults.isEmpty()) {
            aiResults = jobRepository.findByStatus("OPEN", PageRequest.of(0, topK)).getContent();
        }
        return aiResults;
    }

    private String buildPreferenceText(UserEntity user) {
        StringBuilder sb = new StringBuilder();
        if (user.getDesiredTitle() != null && !user.getDesiredTitle().isBlank()) {
            sb.append("期望岗位:").append(user.getDesiredTitle()).append(" ");
        }
        if (user.getDesiredLocation() != null && !user.getDesiredLocation().isBlank()) {
            sb.append("期望地点:").append(user.getDesiredLocation()).append(" ");
        }
        if (user.getDesiredSalary() != null && !user.getDesiredSalary().isBlank()) {
            sb.append("期望薪资:").append(user.getDesiredSalary()).append(" ");
        }
        return sb.toString().trim();
    }
}
