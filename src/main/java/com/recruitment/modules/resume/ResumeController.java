package com.recruitment.modules.resume;

import com.recruitment.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/upload")
    public Result<ResumeEntity> upload(HttpServletRequest request,
                                        @RequestParam("file") MultipartFile file) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(resumeService.upload(userId, file));
    }

    @GetMapping("/{id}")
    public Result<ResumeEntity> detail(@PathVariable Long id) {
        return Result.success(resumeService.getById(id));
    }

    @GetMapping("/list")
    public Result<List<ResumeEntity>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(resumeService.listByUser(userId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        resumeService.delete(id);
        return Result.success(null);
    }
}
