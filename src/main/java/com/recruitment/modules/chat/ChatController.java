package com.recruitment.modules.chat;

import com.recruitment.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void handleWebSocketMessage(ChatMessage msg) {
        ChatMessageEntity saved = chatService.send(msg.senderId(), msg.receiverId(), msg.content());
        messagingTemplate.convertAndSendToUser(
            String.valueOf(msg.receiverId()), "/queue/messages", saved);
    }

    @GetMapping("/contacts")
    public Result<List<Long>> contacts(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(chatService.getContacts(userId));
    }

    @GetMapping("/history/{userId}")
    public Result<Page<ChatMessageEntity>> history(HttpServletRequest request,
                                                    @PathVariable Long userId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "30") int size) {
        Long currentUserId = (Long) request.getAttribute("userId");
        return Result.success(chatService.getHistory(currentUserId, userId, page, size));
    }

    @PostMapping("/send")
    public Result<ChatMessageEntity> send(HttpServletRequest request, @RequestBody SendReq req) {
        Long senderId = (Long) request.getAttribute("userId");
        ChatMessageEntity saved = chatService.send(senderId, req.receiverId(), req.content());
        messagingTemplate.convertAndSendToUser(
            String.valueOf(req.receiverId()), "/queue/messages", saved);
        return Result.success(saved);
    }

    public record ChatMessage(Long senderId, Long receiverId, String content) {}
    public record SendReq(Long receiverId, String content) {}
}
