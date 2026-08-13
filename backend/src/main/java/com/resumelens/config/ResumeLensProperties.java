package com.resumelens.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "resumelens")
public record ResumeLensProperties(
        long maxFileBytes,
        double classificationThreshold,
        int rateLimitPerMinute,
        Embedding embedding,
        Llm llm
) {
    public record Embedding(String modelPath, String vocabPath, String modelName) { }
    public record Llm(boolean enabled, String command, String modelName) { }
}
