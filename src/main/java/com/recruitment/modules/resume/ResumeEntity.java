package com.recruitment.modules.resume;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_resume")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 255)
    private String fileName;

    @Column(length = 500)
    private String storageKey;

    @Column(columnDefinition = "TEXT")
    private String parsedText;

    @Column(length = 20)
    @Builder.Default
    private String status = "PARSED";

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
