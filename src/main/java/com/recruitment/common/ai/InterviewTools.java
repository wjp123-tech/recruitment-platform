package com.recruitment.common.ai;

import com.recruitment.modules.resume.ResumeEntity;
import com.recruitment.modules.resume.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewTools {

    private final ResumeRepository resumeRepository;

    public record LookupRequest(String keyword, long resumeId) {}
    public record CandidateInfoRequest(long resumeId) {}

    public String lookupResume(LookupRequest req) {
        ResumeEntity resume = resumeRepository.findById(req.resumeId()).orElse(null);
        if (resume == null) return "未找到该简历";

        String fullText = resume.getParsedText();
        String keyword = req.keyword();
        if (keyword != null && !keyword.isBlank()) {
            String[] lines = fullText.split("\n");
            List<String> matched = Arrays.stream(lines)
                .filter(line -> line.toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
            if (matched.isEmpty()) {
                return "简历中未找到与「" + keyword + "」相关的内容。简历摘要：\n"
                    + truncate(fullText, 1000);
            }
            return "简历中与「" + keyword + "」相关的内容：\n" + String.join("\n", matched);
        }
        return "候选人简历摘要：\n" + truncate(fullText, 1500);
    }

    public String getCandidateInfo(CandidateInfoRequest req) {
        ResumeEntity resume = resumeRepository.findById(req.resumeId()).orElse(null);
        if (resume == null) return "未找到该简历";

        return String.format("""
            候选人信息：
            - 简历文件：%s
            - 简历字数：%d 字符
            - 上传时间：%s
            """,
            resume.getFileName(),
            resume.getParsedText() != null ? resume.getParsedText().length() : 0,
            resume.getCreatedAt() != null ? resume.getCreatedAt().toString() : "未知");
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }
}
