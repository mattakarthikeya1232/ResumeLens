package com.resumelens.llm;

import com.resumelens.config.ResumeLensProperties;
import com.resumelens.dto.Explanation;
import com.resumelens.dto.LocalLlmSettings;
import com.resumelens.model.ClassificationOutput;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Optional adapter for a trusted local model runner. It deliberately passes only extracted evidence. */
@Component
public class LocalLlmService implements LlmService {
    private final ResumeLensProperties.Llm configuration;
    private final AtomicBoolean enabled = new AtomicBoolean(true);
    public LocalLlmService(ResumeLensProperties properties) { this.configuration = properties.llm(); }
    /** Whether a trusted adapter is configured and executable, regardless of the runtime preference. */
    public boolean isAdapterAvailable() {
        if (!configuration.enabled() || configuration.command() == null || configuration.command().isBlank()) return false;
        File executable = new File(configuration.command().trim().split("\\s+", 2)[0]);
        return executable.isFile() && executable.canExecute();
    }
    @Override public boolean isAvailable() { return enabled.get() && isAdapterAvailable(); }
    public LocalLlmSettings settings() { return new LocalLlmSettings(isAdapterAvailable(), isAvailable()); }
    public LocalLlmSettings updateEnabled(boolean requestedEnabled) {
        enabled.set(requestedEnabled && isAdapterAvailable());
        return settings();
    }
    @Override public String modelName() { return configuration.modelName(); }
    @Override public Explanation explain(ClassificationOutput analysis) {
        if (!isAvailable()) throw new IllegalStateException("Local LLM is not configured");
        String prompt = GroundedPrompt.from(analysis);
        try {
            Process process = new ProcessBuilder(configuration.command().trim().split("\\s+")).start();
            process.getOutputStream().write(prompt.getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().close();
            if (!process.waitFor(25, TimeUnit.SECONDS) || process.exitValue() != 0) throw new IllegalStateException("Local LLM did not finish successfully");
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (output.isBlank()) throw new IllegalStateException("Local LLM returned no explanation");
            return new Explanation(output, "See grounded summary.", "See grounded summary.", java.util.List.of(), java.util.List.of(), "Grounded local LLM response.", "Local LLM: " + modelName());
        } catch (Exception exception) {
            throw new IllegalStateException("Local LLM unavailable", exception);
        }
    }

    private static final class GroundedPrompt {
        static String from(ClassificationOutput analysis) {
            String evidence = analysis.evidence().stream().limit(20).map(item -> "- " + item.classification() + ": " + item.skill() + " | " + item.text()).reduce("", (a, b) -> a + "\n" + b);
            return "Use only this resume evidence. Do not infer names, employers, education, accomplishments, or skills not listed. If a point lacks evidence, say exactly: Insufficient evidence in the resume. Provide a concise candidate profile.\nEVIDENCE:" + evidence;
        }
    }
}
