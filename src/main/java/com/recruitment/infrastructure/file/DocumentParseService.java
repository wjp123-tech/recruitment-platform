package com.recruitment.infrastructure.file;

import com.recruitment.common.exception.BusinessException;
import com.recruitment.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Slf4j
@Service
public class DocumentParseService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/msword",
        "text/plain",
        "text/markdown"
    );

    private final Tika tika = new Tika();
    private final AutoDetectParser parser = new AutoDetectParser();

    public String parseContent(MultipartFile file) {
        String contentType = detectType(file);
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_SUPPORTED,
                "不支持的文件类型: " + contentType);
        }
        try (InputStream in = file.getInputStream()) {
            var handler = new BodyContentHandler(-1);
            var metadata = new org.apache.tika.metadata.Metadata();
            parser.parse(in, handler, metadata);
            String text = handler.toString().trim();
            log.info("文档解析完成: {} -> {} 字符", file.getOriginalFilename(), text.length());
            return text;
        } catch (IOException | SAXException | TikaException e) {
            throw new BusinessException(ErrorCode.RESUME_PARSE_FAILED, "文档解析失败: " + e.getMessage());
        }
    }

    public String detectType(MultipartFile file) {
        try {
            return tika.detect(file.getInputStream());
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}
