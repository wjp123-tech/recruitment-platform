package com.recruitment.common.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class LlmProviderRegistry {

    private final ChatClient.Builder chatClientBuilder;
    private final EmbeddingModel embeddingModel;
    private final LlmProviderProperties properties;
    private final ToolCallbackProvider toolCallbackProvider;

    @Autowired
    public LlmProviderRegistry(ChatClient.Builder chatClientBuilder,
                                EmbeddingModel embeddingModel,
                                LlmProviderProperties properties,
                                @Autowired(required = false) ToolCallbackProvider toolCallbackProvider) {
        this.chatClientBuilder = chatClientBuilder;
        this.embeddingModel = embeddingModel;
        this.properties = properties;
        this.toolCallbackProvider = toolCallbackProvider;
    }

    /** 带 Tool Calling 的面试专用 ChatClient */
    public ChatClient getInterviewChatClient() {
        ChatClient.Builder builder = chatClientBuilder.build().mutate();
        if (toolCallbackProvider != null) {
            builder.defaultToolCallbacks(toolCallbackProvider.getToolCallbacks());
        }
        applyAdvisors(builder);
        return builder.build();
    }

    public ChatClient getDefaultChatClient() {
        return buildClient();
    }

    public ChatClient getPlainChatClient(String providerId) {
        // 无工具、无记忆的简化版 ChatClient
        ChatClient.Builder builder = chatClientBuilder.build().mutate();
        if (properties.getAdvisors() != null && properties.getAdvisors().isSafeguardEnabled()) {
            builder.defaultAdvisors(
                SafeGuardAdvisor.builder()
                    .sensitiveWords(properties.getAdvisors().getSafeguardWords())
                    .failureResponse("抱歉，我只能协助招聘相关任务。")
                    .build()
            );
        }
        return builder.build();
    }

    public ChatClient getChatClient(String providerId) {
        return buildClient();
    }

    public ChatClient getDefaultChatClientOrDefault() {
        return buildClient();
    }

    public EmbeddingModel getDefaultEmbeddingModel() {
        return embeddingModel;
    }

    private ChatClient buildClient() {
        ChatClient.Builder builder = chatClientBuilder.build().mutate();
        applyAdvisors(builder);
        return builder.build();
    }

    private void applyAdvisors(ChatClient.Builder builder) {
        LlmProviderProperties.AdvisorConfig cfg = properties.getAdvisors();
        if (cfg != null && cfg.isEnabled()) {
            if (cfg.isSafeguardEnabled()) {
                builder.defaultAdvisors(
                    SafeGuardAdvisor.builder()
                        .sensitiveWords(cfg.getSafeguardWords())
                        .failureResponse("抱歉，我只能协助招聘相关任务。")
                        .build()
                );
            }
            if (cfg.isMessageChatMemoryEnabled()) {
                int max = Math.max(10, cfg.getMessageChatMemoryMaxMessages());
                builder.defaultAdvisors(
                    MessageChatMemoryAdvisor.builder(
                        MessageWindowChatMemory.builder().maxMessages(max).build()
                    ).build()
                );
            }
        }
    }
}
