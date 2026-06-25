package com.recruitment.modules.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository messageRepository;

    public ChatMessageEntity send(Long senderId, Long receiverId, String content) {
        ChatMessageEntity msg = ChatMessageEntity.builder()
            .senderId(senderId)
            .receiverId(receiverId)
            .content(content)
            .build();
        return messageRepository.save(msg);
    }

    public Page<ChatMessageEntity> getHistory(Long userId1, Long userId2, int page, int size) {
        return messageRepository.findConversation(userId1, userId2,
            PageRequest.of(page, size));
    }

    public List<Long> getContacts(Long userId) {
        return messageRepository.findContactIds(userId);
    }
}
