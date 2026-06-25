package com.recruitment.modules.exam;

import com.recruitment.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @PostMapping("/generate")
    public Result<ExamEntity> generate(HttpServletRequest request, @RequestBody GenerateReq req) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(examService.generate(userId, req.jobId(), req.config()));
    }

    @GetMapping("/{examId}")
    public Result<Map<String, Object>> detail(@PathVariable Long examId) {
        ExamEntity exam = examService.getExam(examId);
        List<ExamService.Question> questions = examService.getQuestions(examId);
        return Result.success(Map.of(
            "exam", exam,
            "questions", questions
        ));
    }

    @PostMapping("/{examId}/submit")
    public Result<Void> submit(@PathVariable Long examId, @RequestBody Map<String, Object> body) {
        // body: {"answers": {"0": "B", "1": "简答题答案...", ...}}
        @SuppressWarnings("unchecked")
        Map<String, Object> raw = (Map<String, Object>) body.get("answers");
        Map<Integer, String> answers = new java.util.HashMap<>();
        if (raw != null) {
            raw.forEach((k, v) -> answers.put(Integer.parseInt(k), String.valueOf(v)));
        }
        examService.submit(examId, answers != null ? answers : Map.of());
        return Result.success(null);
    }

    @GetMapping("/{examId}/result")
    public Result<Map<String, Object>> result(@PathVariable Long examId) {
        ExamEntity exam = examService.getExam(examId);
        List<ExamService.Question> questions = examService.getQuestions(examId);
        List<ExamAnswerEntity> answers = examService.getAnswers(examId);
        return Result.success(Map.of(
            "exam", exam,
            "questions", questions,
            "answers", answers
        ));
    }

    @GetMapping("/{examId}/remaining")
    public Result<Long> remainingTime(@PathVariable Long examId) {
        return Result.success(examService.getRemainingSeconds(examId));
    }

    @GetMapping("/history")
    public Result<List<ExamEntity>> history(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(examService.listHistory(userId));
    }

    record GenerateReq(Long jobId, ExamService.ExamConfig config) {}
}
