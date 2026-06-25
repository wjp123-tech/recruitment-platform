package com.recruitment.modules.interview;

import com.recruitment.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/sessions")
    public Result<InterviewSessionEntity> createSession(HttpServletRequest request,
                                                         @RequestBody CreateReq req) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(interviewService.createSession(userId, req.jobId(), req.resumeId(), req.config()));
    }

    @PostMapping("/sessions/{id}/start")
    public Result<Map<String, String>> start(@PathVariable Long id) {
        return Result.success(Map.of("opening", interviewService.startInterview(id)));
    }

    @PostMapping(value = "/sessions/{id}/answer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> answer(@PathVariable Long id, @RequestBody AnswerReq req) {
        return interviewService.answer(id, req.answer())
            .map(chunk -> ServerSentEvent.<String>builder()
                .data(chunk.replace("\n", "\\n"))
                .build());
    }

    @PostMapping("/sessions/{id}/end")
    public Result<InterviewReportEntity> end(@PathVariable Long id) {
        return Result.success(interviewService.endInterview(id));
    }

    @GetMapping("/sessions")
    public Result<List<InterviewSessionEntity>> listSessions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(interviewService.listSessions(userId));
    }

    @GetMapping("/sessions/{id}/report")
    public Result<InterviewReportEntity> getReport(@PathVariable Long id) {
        return Result.success(interviewService.getReport(id));
    }

    record CreateReq(Long jobId, Long resumeId, InterviewService.InterviewConfig config) {}
    record AnswerReq(String answer) {}
}
