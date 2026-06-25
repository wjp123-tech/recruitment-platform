package com.recruitment.modules.resume;

import com.recruitment.common.exception.BusinessException;
import com.recruitment.common.exception.ErrorCode;
import com.recruitment.infrastructure.file.DocumentParseService;
import com.recruitment.infrastructure.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final FileStorageService fileStorageService;
    private final DocumentParseService documentParseService;

    public ResumeEntity upload(Long userId, MultipartFile file) {
        String parsedText = documentParseService.parseContent(file);
        String storageKey = fileStorageService.store(file);

        ResumeEntity resume = ResumeEntity.builder()
            .userId(userId)
            .fileName(file.getOriginalFilename())
            .storageKey(storageKey)
            .parsedText(parsedText)
            .status("PARSED")
            .build();
        return resumeRepository.save(resume);
    }

    public ResumeEntity getById(Long resumeId) {
        return resumeRepository.findById(resumeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));
    }

    public List<ResumeEntity> listByUser(Long userId) {
        return resumeRepository.findByUserId(userId);
    }

    public void delete(Long resumeId) {
        ResumeEntity resume = getById(resumeId);
        fileStorageService.delete(resume.getStorageKey());
        resumeRepository.delete(resume);
    }
}
