package com.recruitment.modules.exam;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_exam_answer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long examId;

    @Column(nullable = false)
    private Integer questionIndex;

    @Column(columnDefinition = "TEXT")
    private String userAnswer;

    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    private LocalDateTime gradedAt;
}
