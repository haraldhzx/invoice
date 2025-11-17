package com.invoiceapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.llm")
@Data
public class LlmProperties {
    private String provider = "openai"; // openai, anthropic, google
    private OpenAiProperties openai = new OpenAiProperties();
    private AnthropicProperties anthropic = new AnthropicProperties();
    private GoogleProperties google = new GoogleProperties();

    @Data
    public static class OpenAiProperties {
        private String apiKey;
        private String model = "gpt-4-vision-preview";
        private Integer maxTokens = 1000;
    }

    @Data
    public static class AnthropicProperties {
        private String apiKey;
        private String model = "claude-3-sonnet-20240229";
    }

    @Data
    public static class GoogleProperties {
        private String apiKey;
        private String model = "gemini-pro-vision";
    }
}
