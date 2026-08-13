package com.resumelens.service;

import com.resumelens.config.ResumeLensProperties;
import com.resumelens.dto.Diagnostics;
import com.resumelens.llm.LocalLlmService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DiagnosticsService {
    private final ResumeLensProperties properties;
    private final LocalLlmService llm;
    private final OnnxEmbeddingService embedding;
    private final Runtime runtime = Runtime.getRuntime();
    private final AtomicLong sampledPeakHeapBytes = new AtomicLong();
    public DiagnosticsService(ResumeLensProperties properties, LocalLlmService llm, OnnxEmbeddingService embedding) { this.properties = properties; this.llm = llm; this.embedding = embedding; }

    public Diagnostics snapshot(long extractionMs, long inferenceMs, long durationMs) {
        sampleHeap();
        long used = bytesToMb(currentHeapBytes());
        long max = bytesToMb(runtime.maxMemory());
        long peak = bytesToMb(sampledPeakHeapBytes.get());
        String llmStatus = llm.isAvailable() ? "Available" : llm.isAdapterAvailable()
                ? "Disabled by local policy — deterministic fallback active"
                : "Not configured — deterministic fallback active";
        return new Diagnostics(properties.embedding().modelName(), embedding.status(), llm.modelName(), llmStatus, inferenceMs, extractionMs, durationMs, used, max, peak, Instant.now());
    }

    public void sampleHeap() { sampledPeakHeapBytes.accumulateAndGet(currentHeapBytes(), Math::max); }
    private long currentHeapBytes() { return runtime.totalMemory() - runtime.freeMemory(); }
    private long bytesToMb(long value) { return Math.round(value / 1024d / 1024d); }
}
