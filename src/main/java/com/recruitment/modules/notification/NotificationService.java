package com.recruitment.modules.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void notify(Long receiverId, String type, String title, String content, Long refId) {
        NotificationEntity n = NotificationEntity.builder()
            .receiverId(receiverId)
            .type(type)
            .title(title)
            .content(content)
            .refId(refId)
            .build();
        notificationRepository.save(n);
        log.info("通知已发送: receiver={}, type={}", receiverId, type);
    }

    public Page<NotificationEntity> list(Long receiverId, int page, int size) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId,
            PageRequest.of(page, size));
    }

    public long unreadCount(Long receiverId) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(receiverId);
    }

    @Transactional
    public void markRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllRead(Long receiverId) {
        notificationRepository.markAllRead(receiverId);
    }
}
