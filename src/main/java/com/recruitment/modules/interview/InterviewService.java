package com.recruitment.modules.interview;

import com.recruitment.common.ai.LlmProviderRegistry;
import com.recruitment.common.exception.BusinessException;
import com.recruitment.common.exception.ErrorCode;
import com.recruitment.modules.interview.InterviewService.InterviewConfig;
import com.recruitment.modules.job.JobEntity;
import com.recruitment.modules.job.JobRepository;
import com.recruitment.modules.notification.NotificationService;
import com.recruitment.modules.resume.ResumeEntity;
import com.recruitment.modules.resume.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final InterviewMessageRepository messageRepository;
    private final InterviewReportRepository reportRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final LlmProviderRegistry llmProviderRegistry;
    private final NotificationService notificationService;
    private final ResourceLoader resourceLoader;

    @Transactional
    public InterviewSessionEntity createSession(Long userId, Long jobId, Long resumeId, InterviewConfig config) {
        JobEntity job = jobRepository.findById(jobId)
            .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));
        ResumeEntity resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

        InterviewSessionEntity session = InterviewSessionEntity.builder()
            .userId(userId)
            .jobId(jobId)
            .resumeId(resumeId)
            .maxRounds(config != null && config.maxRounds() != null ? config.maxRounds() : 10)
            .build();
        return sessionRepository.save(session);
    }

    public String startInterview(Long sessionId) {
        InterviewSessionEntity session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));
        if (!"PENDING".equals(session.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "会话状态不允许开始面试");
        }
        session.setStatus("IN_PROGRESS");
        sessionRepository.save(session);

        String opening = generateOpening(session);
        messageRepository.save(InterviewMessageEntity.builder()
            .sessionId(sessionId).role("ASSISTANT").content(opening)
            .round(0).build());
        return opening;
    }

    public Flux<String> answer(Long sessionId, String userAnswer) {
        InterviewSessionEntity session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));
        if (!"IN_PROGRESS".equals(session.getStatus())) {
            return Flux.error(new BusinessException(ErrorCode.BAD_REQUEST, "面试已结束或未开始"));
        }

        int round = session.getCurrentRound() + 1;
        session.setCurrentRound(round);
        sessionRepository.save(session);

        messageRepository.save(InterviewMessageEntity.builder()
            .sessionId(sessionId).role("USER").content(userAnswer).round(round).build());

        if (round >= session.getMaxRounds()) {
            return Flux.just("面试即将结束，感谢你的参与。");
        }

        List<InterviewMessageEntity> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        JobEntity job = jobRepository.findById(session.getJobId()).orElse(null);
        ResumeEntity resume = resumeRepository.findById(session.getResumeId()).orElse(null);

        // 使用带 Tool Calling 的面试专用 ChatClient
        ChatClient chatClient = llmProviderRegistry.getInterviewChatClient();
        StringBuilder sysPrompt = new StringBuilder();
        sysPrompt.append(String.format("""
            你是%s岗位的面试官。根据以下信息对候选人进行面试。

            ## 岗位要求
            %s

            ## 候选人简历摘要
            %s

            ## 可用工具
            你拥有两个工具可以随时调用：
            - lookupResume(keyword, resumeId)：搜索候选人简历中的关键字相关内容（如项目经历、技术栈、工作年限）
            - getCandidateInfo(resumeId)：获取候选人基本信息

            **重要：请主动使用工具。** 在以下场景务必调用工具：
            1. 想深入了解简历中提到的某个技术、项目或经历时
            2. 候选人回答中提到简历没有的技能，需要核实简历时
            3. 开场时先调 getCandidateInfo 确认候选人身份
            4. 不要仅仅依赖简历摘要，简历摘要只是概览，细节需要通过工具获取

            ## 面试规则
            1. 回复简洁（2-4句话），追问具体、有深度
            2. 每个问题聚焦一个技能点或项目经历
            3. 当前轮次: %d/%d，接近尾声时给出总结
            4. 候选人当前简历ID: %d
            """,
            job != null ? job.getTitle() : "某公司",
            job != null ? job.getRequirements() : "",
            resume != null ? (resume.getParsedText().length() > 600
                ? resume.getParsedText().substring(0, 600) + "..." : resume.getParsedText()) : "",
            round, session.getMaxRounds(),
            resume != null ? resume.getId() : 0
        ));

        List<Message> msgHistory = new ArrayList<>();
        msgHistory.add(new SystemMessage(sysPrompt.toString()));
        for (InterviewMessageEntity m : history) {
            if ("USER".equals(m.getRole())) {
                msgHistory.add(new UserMessage(m.getContent()));
            } else {
                msgHistory.add(new AssistantMessage(m.getContent()));
            }
        }
        msgHistory.add(new UserMessage(userAnswer));
        return chatClient.prompt().messages(msgHistory).stream().content();
    }

    public InterviewReportEntity endInterview(Long sessionId) {
        InterviewSessionEntity session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));
        session.setStatus("COMPLETED");
        sessionRepository.save(session);

        List<InterviewMessageEntity> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        String report = generateEvaluation(session, messages);
        notificationService.notify(session.getUserId(), "INTERVIEW_COMPLETE",
            "面试评估已完成", "你的模拟面试评估报告已生成", sessionId);

        return reportRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_EVALUATION_FAILED));
    }

    public List<InterviewSessionEntity> listSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public InterviewReportEntity getReport(Long sessionId) {
        return reportRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_EVALUATION_FAILED, "评估报告尚未生成"));
    }

    private String generateOpening(InterviewSessionEntity session) {
        JobEntity job = jobRepository.findById(session.getJobId()).orElse(null);
        ResumeEntity resume = resumeRepository.findById(session.getResumeId()).orElse(null);
        ChatClient client = llmProviderRegistry.getPlainChatClient(null);
        try {
            return client.prompt()
                .system(String.format("""
                    你是%s的面试官。请生成一个简短的开场白（2-3句）。
                    包括：简单的自我介绍、表明今天面试的是%s岗位、让候选人放轻松。
                    """, job != null ? job.getTitle() : "某公司", job != null ? job.getTitle() : ""))
                .user("请开始")
                .call()
                .content();
        } catch (Exception e) {
            String title = job != null ? job.getTitle() : "该岗位";
            return "你好，我是今天的AI面试官。很高兴为你进行「" + title + "」的模拟面试，请放松回答就好。";
        }
    }

    private String generateEvaluation(InterviewSessionEntity session, List<InterviewMessageEntity> messages) {
        ChatClient client = llmProviderRegistry.getPlainChatClient(null);
        JobEntity job = jobRepository.findById(session.getJobId()).orElse(null);

        StringBuilder qa = new StringBuilder();
        for (int i = 0; i < messages.size(); i += 2) {
            qa.append("Q: ").append(i < messages.size() ? messages.get(i).getContent() : "").append("\n");
            qa.append("A: ").append(i + 1 < messages.size() ? messages.get(i + 1).getContent() : "").append("\n\n");
        }

        var outputConverter = new BeanOutputConverter<>(EvalResult.class);
        try {
            String result = client.prompt()
                .system("你是专业的面试评估专家。请基于面试对话记录，进行综合评分。\n" + outputConverter.getFormat())
                .user(String.format("岗位: %s\n面试记录:\n%s\n请评估",
                    job != null ? job.getTitle() : "未知岗位", qa.toString()))
                .call()
                .content();
            EvalResult eval = outputConverter.convert(result);

            InterviewReportEntity report = InterviewReportEntity.builder()
                .sessionId(session.getId())
                .overallScore(eval.overallScore())
                .dimensions(toJson(eval))
                .strengths(String.join(";", eval.strengths()))
                .weaknesses(String.join(";", eval.weaknesses()))
                .suggestions(eval.suggestions())
                .build();
            return reportRepository.save(report).getSuggestions(); // trigger save
        } catch (Exception e) {
            log.error("评估生成失败: {}", e.getMessage());
            InterviewReportEntity fallback = InterviewReportEntity.builder()
                .sessionId(session.getId())
                .overallScore(70)
                .dimensions("{}")
                .strengths("完成了面试")
                .weaknesses("评估失败，请重试")
                .suggestions("系统错误，请重新发起面试评估")
                .build();
            reportRepository.save(fallback);
            throw new BusinessException(ErrorCode.INTERVIEW_EVALUATION_FAILED);
        }
    }

    private String toJson(EvalResult eval) {
        return String.format("{\"scores\":{\"技术能力\":%d,\"沟通表达\":%d,\"项目经验\":%d,\"综合素质\":%d}}",
            eval.techScore(), eval.commScore(), eval.projectScore(), eval.overallScore());
    }

    public record InterviewConfig(Integer maxRounds) {}
    record EvalResult(int overallScore, int techScore, int commScore, int projectScore,
                      List<String> strengths, List<String> weaknesses, String suggestions) {}
}
