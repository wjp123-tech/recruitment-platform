package com.recruitment.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_ALREADY_EXISTS(1003, "用户已存在"),

    JOB_NOT_FOUND(2001, "岗位不存在"),
    JOB_PERMISSION_DENIED(2002, "仅能操作自己的岗位"),

    RESUME_NOT_FOUND(3001, "简历不存在"),
    RESUME_PARSE_FAILED(3002, "简历解析失败"),

    DELIVERY_NOT_FOUND(4001, "投递记录不存在"),
    DELIVERY_DUPLICATE(4002, "请勿重复投递"),

    INTERVIEW_SESSION_NOT_FOUND(5001, "面试会话不存在"),
    INTERVIEW_QUESTION_FAILED(5002, "生成面试题失败"),
    INTERVIEW_EVALUATION_FAILED(5003, "面试评估失败"),

    EXAM_NOT_FOUND(6001, "试卷不存在"),
    EXAM_GENERATE_FAILED(6002, "生成试卷失败"),
    EXAM_GRADE_FAILED(6003, "批改失败"),

    AI_SERVICE_ERROR(7001, "AI 服务异常"),
    AI_SERVICE_TIMEOUT(7002, "AI 服务超时"),

    FILE_UPLOAD_FAILED(8001, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED(8002, "不支持的文件类型");

    private final int code;
    private final String description;

    ErrorCode(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
