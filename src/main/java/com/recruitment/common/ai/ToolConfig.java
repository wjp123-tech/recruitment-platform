package com.recruitment.common.ai;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class ToolConfig {

    @Bean
    public ToolCallbackProvider interviewToolCallbackProvider(InterviewTools tools) {
        return () -> {
            ToolCallback lookupResume = FunctionToolCallback
                .builder("lookupResume",
                    (Function<InterviewTools.LookupRequest, String>) tools::lookupResume)
                .description("搜索候选人简历中与关键字相关的内容（项目经历、技术栈等）。" +
                    "参数 keyword 为搜索关键字，resumeId 为简历ID。")
                .inputType(InterviewTools.LookupRequest.class)
                .build();

            ToolCallback getCandidateInfo = FunctionToolCallback
                .builder("getCandidateInfo",
                    (Function<InterviewTools.CandidateInfoRequest, String>) tools::getCandidateInfo)
                .description("获取候选人的基本信息（简历文件名、字数、上传时间）。" +
                    "参数 resumeId 为简历ID。")
                .inputType(InterviewTools.CandidateInfoRequest.class)
                .build();

            return new ToolCallback[] { lookupResume, getCandidateInfo };
        };
    }
}
