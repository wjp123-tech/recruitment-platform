package com.recruitment.modules.delivery;

import com.recruitment.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/apply")
    public Result<DeliveryEntity> apply(HttpServletRequest request,
                                         @RequestBody ApplyReq req) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(deliveryService.apply(userId, req.jobId(), req.resumeId()));
    }

    @GetMapping("/my")
    public Result<Page<DeliveryEntity>> myDeliveries(HttpServletRequest request,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(deliveryService.myDeliveries(userId, page, size));
    }

    @GetMapping("/job/{jobId}")
    public Result<List<DeliveryDTO>> jobDeliveries(@PathVariable Long jobId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return Result.success(deliveryService.jobDeliveries(jobId, page, size));
    }

    @GetMapping("/{id}/resume")
    public Result<Map<String, Object>> viewResume(@PathVariable Long id) {
        return Result.success(deliveryService.getResumeContent(id));
    }

    @PutMapping("/{id}/status")
    public Result<DeliveryEntity> updateStatus(@PathVariable Long id, @RequestBody StatusReq req) {
        return Result.success(deliveryService.updateStatus(id, req.status(), req.remark()));
    }

    public record ApplyReq(Long jobId, Long resumeId) {}
    public record StatusReq(String status, String remark) {}
}
