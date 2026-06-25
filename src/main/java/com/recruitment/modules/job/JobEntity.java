package com.recruitment.modules.job;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_job")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long recruiterId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(length = 50)
    private String salaryRange;

    @Column(length = 100)
    private String location;

    @Column(length = 20)
    private String jobType;

    @Column(length = 20)
    @Builder.Default
    private String status = "OPEN";

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = "OPEN";
    }
}
