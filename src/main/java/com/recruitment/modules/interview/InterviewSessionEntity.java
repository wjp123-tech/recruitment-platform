package com.recruitment.modules.interview;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_interview_session")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long jobId;

    @Column(nullable = false)
    private Long resumeId;

    @Builder.Default
    private Integer maxRounds = 10;

    @Builder.Default
    private Integer currentRound = 0;

    @Column(length = 20)
    @Builder.Default
    private String status = "PENDING";

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
