package com.recruitment.modules.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    @Query("SELECT m FROM ChatMessageEntity m WHERE "
        + "(m.senderId = :userId1 AND m.receiverId = :userId2) "
        + "OR (m.senderId = :userId2 AND m.receiverId = :userId1) "
        + "ORDER BY m.createdAt DESC")
    Page<ChatMessageEntity> findConversation(Long userId1, Long userId2, Pageable pageable);

    @Query("SELECT DISTINCT CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END "
        + "FROM ChatMessageEntity m WHERE m.senderId = :userId OR m.receiverId = :userId")
    List<Long> findContactIds(Long userId);
}
