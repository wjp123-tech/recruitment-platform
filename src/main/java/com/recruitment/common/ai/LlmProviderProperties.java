package com.recruitment.common.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class LlmProviderProperties {
    private String defaultProvider;
    private int embeddingDimensions = 1024;
    private AdvisorConfig advisors = new AdvisorConfig();
    private Map<String, ProviderConfig> providers;

    @Data
    public static class AdvisorConfig {
        private boolean enabled = true;
        private boolean safeguardEnabled = true;
        private List<String> safeguardWords = List.of();
        private boolean messageChatMemoryEnabled = true;
        private int messageChatMemoryMaxMessages = 20;
        private boolean simpleLoggerEnabled = false;
        private boolean toolCallEnabled = false;
        private boolean toolCallConversationHistoryEnabled = true;
        private boolean streamToolCallResponses = true;
    }

    @Data
    public static class ProviderConfig {
        private String baseUrl;
        private String apiKey;
        private String model;
        private String embeddingModel;
        private Boolean supportsEmbedding;
        private Double temperature;
    }
}
