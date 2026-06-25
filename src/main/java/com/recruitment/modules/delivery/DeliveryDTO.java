package com.recruitment.modules.delivery;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DeliveryDTO {
    private Long id;
    private Long jobId;
    private Long resumeId;
    private String resumeFileName;
    private Long jobSeekerId;
    private String jobSeekerName;
    private Long recruiterId;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
}
