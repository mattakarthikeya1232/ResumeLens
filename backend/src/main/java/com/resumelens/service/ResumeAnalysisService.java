package com.resumelens.service;

import com.resumelens.config.ResumeLensProperties;
import com.resumelens.dto.*;
import com.resumelens.exception.ApiException;
import com.resumelens.llm.DeterministicExplanationService;
import com.resumelens.llm.LocalLlmService;
import com.resumelens.model.ClassificationOutput;
import com.resumelens.parser.DocumentTextExtractor;
import com.resumelens.parser.SectionDetector;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ResumeAnalysisService {
    private final List<DocumentTextExtractor> extractors;
    private final SectionDetector sectionDetector;
    private final ClassifierService classifier;
    private final DeterministicExplanationService fallbackExplanation;
    private final LocalLlmService localLlm;
    private final DiagnosticsService diagnostics;
    private final ResumeLensProperties properties;
    private final Map<String, AnalysisResult> analyses = new ConcurrentHashMap<>();

    public ResumeAnalysisService(List<DocumentTextExtractor> extractors, SectionDetector sectionDetector, ClassifierService classifier,
                                 DeterministicExplanationService fallbackExplanation, LocalLlmService localLlm,
                                 DiagnosticsService diagnostics, ResumeLensProperties properties) {
        this.extractors = extractors; this.sectionDetector = sectionDetector; this.classifier = classifier;
        this.fallbackExplanation = fallbackExplanation; this.localLlm = localLlm; this.diagnostics = diagnostics; this.properties = properties;
    }

    public AnalysisResult analyze(MultipartFile file) {
        long started = System.nanoTime();
        diagnostics.sampleHeap();
        validate(file);
        String filename = safeFilename(file.getOriginalFilename());
        byte[] contents;
        try { contents = file.getBytes(); } catch (IOException exception) { throw new ApiException(HttpStatus.BAD_REQUEST, "Unable to read the uploaded resume."); }
        validateSignature(filename, contents);
        diagnostics.sampleHeap();
        long extractionStarted = System.nanoTime();
        DocumentTextExtractor extractor = extractors.stream().filter(item -> item.supports(filename)).findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only PDF and DOCX resumes are supported."));
        String text = normalize(extractor.extract(contents));
        diagnostics.sampleHeap();
        long extractionMs = elapsed(extractionStarted);
        if (text.length() < 20) throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "No usable text was found. This may be an image-only scanned resume.");
        List<SectionDetector.DetectedSection> detected = sectionDetector.detect(text);
        ClassificationOutput output = classifier.classify(detected);
        diagnostics.sampleHeap();
        Explanation explanation = explain(output);
        long duration = elapsed(started);
        String id = UUID.randomUUID().toString();
        AnalysisResult result = new AnalysisResult(id, filename, Instant.now(), "COMPLETED", output.technicalScore(), output.nonTechnicalScore(), output.technicalSkills(), output.nonTechnicalSkills(), sectionDetector.publicSections(detected), output.evidence(), explanation, diagnostics.snapshot(extractionMs, output.inferenceTimeMs(), duration), output.mode());
        analyses.put(id, result);
        return result;
    }

    public AnalysisResult find(String id) {
        AnalysisResult result = analyses.get(id);
        if (result == null) throw new ApiException(HttpStatus.NOT_FOUND, "Analysis not found. It may have expired after the server restarted.");
        return result;
    }
    public List<AnalysisSummary> history() {
        return analyses.values().stream().sorted(Comparator.comparing(AnalysisResult::analyzedAt).reversed())
                .map(item -> new AnalysisSummary(item.analysisId(), item.resumeName(), item.analyzedAt(), item.technicalScore(), item.nonTechnicalScore(), item.status())).toList();
    }
    public void delete(String id) { if (analyses.remove(id) == null) throw new ApiException(HttpStatus.NOT_FOUND, "Analysis not found."); }

    private Explanation explain(ClassificationOutput output) {
        if (localLlm.isAvailable()) {
            try { return localLlm.explain(output); } catch (RuntimeException ignored) { /* factual fallback is intentional */ }
        }
        return fallbackExplanation.explain(output);
    }
    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "Choose a non-empty PDF or DOCX resume.");
        if (file.getSize() > properties.maxFileBytes()) throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "This resume exceeds the 8 MB upload limit.");
        String name = file.getOriginalFilename();
        if (name == null || !(name.toLowerCase().endsWith(".pdf") || name.toLowerCase().endsWith(".docx"))) throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Choose a PDF or DOCX resume.");
    }
    private String normalize(String value) { return value.replace('\u0000', ' ').replaceAll("[\\t ]+", " ").replaceAll("\\n{3,}", "\n\n").trim(); }
    private String safeFilename(String value) { return Optional.ofNullable(value).map(name -> name.replaceAll("[^A-Za-z0-9._ -]", "_")).filter(name -> !name.isBlank()).orElse("resume"); }
    private void validateSignature(String filename, byte[] contents) {
        boolean pdf = filename.toLowerCase(Locale.ROOT).endsWith(".pdf");
        boolean expected = pdf ? contents.length >= 4 && contents[0] == '%' && contents[1] == 'P' && contents[2] == 'D' && contents[3] == 'F'
                : contents.length >= 4 && contents[0] == 'P' && contents[1] == 'K';
        if (!expected) throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "The file content does not match its PDF or DOCX extension.");
    }
    private long elapsed(long started) { return (System.nanoTime() - started) / 1_000_000; }
}
