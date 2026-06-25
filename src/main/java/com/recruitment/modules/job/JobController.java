package com.recruitment.modules.job;

import com.recruitment.common.result.Result;
import com.recruitment.modules.job.JobService.JobCreateReq;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    public Result<Page<JobEntity>> list(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        return Result.success(jobService.listOpen(keyword, page, size));
    }

    @GetMapping("/{id}")
    public Result<JobEntity> detail(@PathVariable Long id) {
        return Result.success(jobService.getById(id));
    }

    @PostMapping
    public Result<JobEntity> create(HttpServletRequest request, @Valid @RequestBody JobCreateReq req) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        if (!"RECRUITER".equals(role)) {
            return Result.error(403, "仅招聘官可以发布岗位");
        }
        return Result.success(jobService.create(userId, req));
    }

    @PutMapping("/{id}")
    public Result<JobEntity> update(HttpServletRequest request,
                                     @PathVariable Long id, @Valid @RequestBody JobCreateReq req) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(jobService.update(userId, id, req));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        jobService.delete(userId, id);
        return Result.success(null);
    }

    @GetMapping("/my")
    public Result<Page<JobEntity>> myJobs(HttpServletRequest request,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(jobService.myJobs(userId, page, size));
    }

    @GetMapping("/recommend")
    public Result<List<JobEntity>> recommend(HttpServletRequest request,
                                              @RequestParam(defaultValue = "10") int topK) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(jobService.recommend(userId, topK));
    }
}
