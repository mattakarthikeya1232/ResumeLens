package com.resumelens.dto;

import java.time.Instant;

public record Diagnostics(
        String embeddingModel,
        String embeddingStatus,
        String llmModel,
        String llmStatus,
        long inferenceTimeMs,
        long documentProcessingTimeMs,
        long analysisDurationMs,
        long usedHeapMb,
        long maxHeapMb,
        long peakMemoryMb,
        Instant collectedAt
) { }
