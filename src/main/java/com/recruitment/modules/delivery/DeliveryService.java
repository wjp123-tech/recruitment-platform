package com.recruitment.modules.delivery;

import com.recruitment.common.exception.BusinessException;
import com.recruitment.common.exception.ErrorCode;
import com.recruitment.infrastructure.file.FileStorageService;
import com.recruitment.modules.job.JobEntity;
import com.recruitment.modules.job.JobRepository;
import com.recruitment.modules.notification.NotificationService;
import com.recruitment.modules.resume.ResumeEntity;
import com.recruitment.modules.resume.ResumeRepository;
import com.recruitment.modules.user.UserEntity;
import com.recruitment.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;

    public DeliveryEntity apply(Long jobSeekerId, Long jobId, Long resumeId) {
        JobEntity job = jobRepository.findById(jobId)
            .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));
        if (deliveryRepository.findByJobIdAndJobSeekerId(jobId, jobSeekerId).isPresent()) {
            throw new BusinessException(ErrorCode.DELIVERY_DUPLICATE);
        }
        resumeRepository.findById(resumeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

        DeliveryEntity delivery = DeliveryEntity.builder()
            .jobId(jobId)
            .resumeId(resumeId)
            .jobSeekerId(jobSeekerId)
            .recruiterId(job.getRecruiterId())
            .build();
        delivery = deliveryRepository.save(delivery);

        notificationService.notify(job.getRecruiterId(), "DELIVERY_NEW",
            "收到新的简历投递", "有求职者投递了岗位「" + job.getTitle() + "」", delivery.getId());
        return delivery;
    }

    public Page<DeliveryEntity> myDeliveries(Long jobSeekerId, int page, int size) {
        return deliveryRepository.findByJobSeekerId(jobSeekerId,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public List<DeliveryDTO> jobDeliveries(Long jobId, int page, int size) {
        Page<DeliveryEntity> pageResult = deliveryRepository.findByJobId(jobId,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return pageResult.getContent().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public DeliveryEntity updateStatus(Long deliveryId, String status, String remark) {
        DeliveryEntity delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DELIVERY_NOT_FOUND));
        delivery.setStatus(status);
        if (remark != null) delivery.setRemark(remark);
        delivery = deliveryRepository.save(delivery);

        JobEntity job = jobRepository.findById(delivery.getJobId()).orElse(null);
        String jobTitle = job != null ? job.getTitle() : "未知岗位";
        String statusText = switch (status) {
            case "REVIEWED" -> "简历已通过筛选";
            case "INTERVIEW" -> "恭喜进入面试环节";
            case "OFFER" -> "恭喜获得录用";
            case "REJECTED" -> "暂时不匹配";
            default -> "状态已更新";
        };
        notificationService.notify(delivery.getJobSeekerId(), "DELIVERY_STATUS",
            "投递状态更新", "您在岗位「" + jobTitle + "」的投递状态: " + statusText, delivery.getId());
        return delivery;
    }

    public Map<String, Object> getResumeContent(Long deliveryId) {
        DeliveryEntity delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DELIVERY_NOT_FOUND));
        ResumeEntity resume = resumeRepository.findById(delivery.getResumeId())
            .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));
        UserEntity user = userRepository.findById(delivery.getJobSeekerId()).orElse(null);

        return Map.of(
            "resumeId", resume.getId(),
            "fileName", resume.getFileName(),
            "parsedText", resume.getParsedText(),
            "jobSeekerName", user != null ? user.getUsername() : "未知",
            "jobSeekerEmail", user != null && user.getEmail() != null ? user.getEmail() : "",
            "jobSeekerPhone", user != null && user.getPhone() != null ? user.getPhone() : ""
        );
    }

    private DeliveryDTO toDTO(DeliveryEntity d) {
        ResumeEntity resume = resumeRepository.findById(d.getResumeId()).orElse(null);
        return DeliveryDTO.builder()
            .id(d.getId())
            .jobId(d.getJobId())
            .resumeId(d.getResumeId())
            .resumeFileName(resume != null ? resume.getFileName() : "未知简历")
            .jobSeekerId(d.getJobSeekerId())
            .jobSeekerName(getUsername(d.getJobSeekerId()))
            .recruiterId(d.getRecruiterId())
            .status(d.getStatus())
            .remark(d.getRemark())
            .createdAt(d.getCreatedAt())
            .build();
    }

    private String getUsername(Long userId) {
        return userRepository.findById(userId)
            .map(UserEntity::getUsername)
            .orElse("用户#" + userId);
    }
}
