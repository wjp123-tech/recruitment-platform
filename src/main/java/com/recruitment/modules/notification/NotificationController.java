package com.recruitment.modules.notification;

import com.recruitment.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/list")
    public Result<Page<NotificationEntity>> list(HttpServletRequest request,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(notificationService.list(userId, page, size));
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(Map.of("count", notificationService.unreadCount(userId)));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.success(null);
    }

    @PutMapping("/read-all")
    public Result<Void> markAllRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAllRead(userId);
        return Result.success(null);
    }
}
