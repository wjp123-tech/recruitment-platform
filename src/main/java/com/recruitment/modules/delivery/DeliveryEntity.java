package com.recruitment.modules.delivery;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_delivery", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"jobId", "jobSeekerId"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long jobId;

    @Column(nullable = false)
    private Long resumeId;

    @Column(nullable = false)
    private Long jobSeekerId;

    @Column(nullable = false)
    private Long recruiterId;

    @Column(length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(length = 500)
    private String remark;

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
