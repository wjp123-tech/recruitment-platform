package com.recruitment.modules.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    Page<NotificationEntity> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    long countByReceiverIdAndIsReadFalse(Long receiverId);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.receiverId = :receiverId")
    void markAllRead(Long receiverId);
}
