package com.recruitment.modules.exam;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.common.ai.LlmProviderRegistry;
import com.recruitment.common.exception.BusinessException;
import com.recruitment.common.exception.ErrorCode;
import com.recruitment.modules.job.JobEntity;
import com.recruitment.modules.job.JobRepository;
import com.recruitment.modules.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamAnswerRepository answerRepository;
    private final JobRepository jobRepository;
    private final LlmProviderRegistry llmProviderRegistry;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public record ExamConfig(int totalCount, int singleChoice, int multiChoice, int essay, int coding, int durationMinutes) {}
    public record Question(String type, String content, List<String> options, String answer,
                           int score, String difficulty, String knowledgePoint) {}
    public record ExamDTO(List<Question> questions) {}

    public ExamEntity generate(Long userId, Long jobId, ExamConfig config) {
        JobEntity job = jobRepository.findById(jobId)
            .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));

        ChatClient client = llmProviderRegistry.getPlainChatClient(null);
        var outputConverter = new BeanOutputConverter<>(ExamDTO.class);

        int total = config != null ? config.totalCount() : 10;
        int sc = config != null && config.singleChoice() > 0 ? config.singleChoice() : total / 2;
        int essay = config != null && config.essay() > 0 ? config.essay() : total - sc;

        try {
            String result = client.prompt()
                .system(String.format("""
                    你是%s的笔试题出题官。请生成笔试试卷。%s
                    """, job.getTitle(), outputConverter.getFormat()))
                .user(String.format("""
                    岗位要求: %s
                    题目数量: %d 题（单选 %d 题、简答 %d 题）
                    难度: 简单30%%、中等50%%、困难20%%
                    """, job.getRequirements(), total, sc, essay))
                .call()
                .content();
            ExamDTO dto = outputConverter.convert(result);
            int totalScore = dto.questions().stream().mapToInt(Question::score).sum();

            int duration = config != null && config.durationMinutes() > 0 ? config.durationMinutes() : 30;
            ExamEntity exam = ExamEntity.builder()
                .userId(userId).jobId(jobId)
                .questions(objectMapper.writeValueAsString(dto.questions()))
                .totalScore(totalScore)
                .durationMinutes(duration)
                .startedAt(java.time.LocalDateTime.now())
                .build();
            return examRepository.save(exam);
        } catch (Exception e) {
            log.error("生成试卷失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.EXAM_GENERATE_FAILED, "生成试卷失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public ExamEntity getExam(Long examId) {
        return examRepository.findById(examId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));
    }

    @SuppressWarnings("unchecked")
    public List<Question> getQuestions(Long examId) {
        ExamEntity exam = getExam(examId);
        try {
            return objectMapper.readValue(exam.getQuestions(), new TypeReference<>() {});
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "试卷解析失败");
        }
    }

    public long getRemainingSeconds(Long examId) {
        ExamEntity exam = getExam(examId);
        if (!"IN_PROGRESS".equals(exam.getStatus()) || exam.getStartedAt() == null) return 0;
        long elapsed = java.time.Duration.between(exam.getStartedAt(), java.time.LocalDateTime.now()).getSeconds();
        long total = (long) exam.getDurationMinutes() * 60;
        return Math.max(0, total - elapsed);
    }

    public void submit(Long examId, Map<Integer, String> answers) {
        ExamEntity exam = getExam(examId);
        if (!"IN_PROGRESS".equals(exam.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "试卷已提交");
        }
        // 超时检查：超过允许时间自动标记为超时提交
        long remaining = getRemainingSeconds(examId);
        if (remaining <= 0) {
            exam.setStatus("TIMEOUT");
        }

        List<Question> questions = getQuestions(examId);
        ChatClient client = llmProviderRegistry.getPlainChatClient(null);

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            String userAns = answers.getOrDefault(i, "");
            ExamAnswerEntity ans = ExamAnswerEntity.builder()
                .examId(examId).questionIndex(i).userAnswer(userAns).build();

            if ("SINGLE_CHOICE".equals(q.type()) || "MULTI_CHOICE".equals(q.type())) {
                ans.setScore(userAns.equalsIgnoreCase(q.answer().trim()) ? q.score() : 0);
                ans.setFeedback(userAns.equalsIgnoreCase(q.answer().trim()) ? "正确" : "正确答案: " + q.answer());
            } else {
                try {
                    String grade = client.prompt()
                        .system("你是阅卷老师。请对以下答案评分(0-" + q.score() + "分)，并给出简短反馈。")
                        .user("题目: " + q.content() + "\n参考答案: " + q.answer() + "\n考生答案: " + userAns)
                        .call()
                        .content();
                    ans.setFeedback(grade);
                    ans.setScore(extractScore(grade, q.score()));
                } catch (Exception e) {
                    ans.setScore(0);
                    ans.setFeedback("批改失败");
                }
            }
            ans.setGradedAt(java.time.LocalDateTime.now());
            answerRepository.save(ans);
        }

        exam.setStatus("GRADED");
        examRepository.save(exam);

        notificationService.notify(exam.getUserId(), "EXAM_GRADED",
            "笔试批改完成", "你的笔试试卷已批改完成", examId);
    }

    public List<ExamAnswerEntity> getAnswers(Long examId) {
        return answerRepository.findByExamId(examId);
    }

    public List<ExamEntity> listHistory(Long userId) {
        return examRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private int extractScore(String text, int maxScore) {
        try {
            String s = text.replaceAll("[^0-9]", " ").trim();
            String[] parts = s.split("\\s+");
            for (String part : parts) {
                int score = Integer.parseInt(part);
                if (score >= 0 && score <= maxScore) return score;
            }
        } catch (Exception ignored) {}
        return maxScore / 2;
    }
}
