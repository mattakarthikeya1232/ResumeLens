package com.resumelens.service;

import ai.onnxruntime.*;
import com.resumelens.config.ResumeLensProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Optional, CPU-only MiniLM adapter. A missing or incompatible model always leaves the
 * deterministic evidence classifier active. The service owns one reusable OrtSession.
 */
@Service
public class OnnxEmbeddingService {
    private static final Logger log = LoggerFactory.getLogger(OnnxEmbeddingService.class);
    private static final int MAX_TOKENS = 128;
    private static final Pattern TOKENS = Pattern.compile("[a-z0-9]+|[^\\s]");
    private final ResumeLensProperties.Embedding configuration;
    private OrtEnvironment environment;
    private OrtSession session;
    private Map<String, Long> vocabulary = Map.of();
    private double[] technicalAnchor;
    private double[] professionalAnchor;
    private String status = "Not configured — contextual rules active";

    public OnnxEmbeddingService(ResumeLensProperties properties) { this.configuration = properties.embedding(); }

    @PostConstruct
    void initialize() {
        if (blank(configuration.modelPath()) || blank(configuration.vocabPath())) return;
        try {
            Path model = Path.of(configuration.modelPath()); Path vocab = Path.of(configuration.vocabPath());
            if (!Files.isRegularFile(model) || !Files.isRegularFile(vocab)) { status = "Model or vocabulary path is unavailable — contextual rules active"; return; }
            vocabulary = loadVocabulary(vocab);
            environment = OrtEnvironment.getEnvironment();
            session = environment.createSession(model.toString(), new OrtSession.SessionOptions());
            technicalAnchor = embed("software engineering programming languages frameworks databases cloud devops machine learning");
            professionalAnchor = embed("communication leadership teamwork collaboration management presentation organization problem solving");
            status = "Loaded — MiniLM sentence similarity active";
            log.info("Loaded local ONNX embedding model with {} vocabulary entries", vocabulary.size());
        } catch (Exception exception) {
            closeSession(); status = "Unable to load model — contextual rules active";
            log.warn("ONNX embedding model could not be loaded; using fallback classification", exception);
        }
    }

    public boolean isAvailable() { return session != null && technicalAnchor != null && professionalAnchor != null; }
    public String status() { return status; }
    public String mode() { return isAvailable() ? "ONNX MiniLM semantic similarity + evidence rules" : "Evidence-weighted rules fallback"; }

    public OptionalDouble relevance(String text, String classification) {
        if (!isAvailable()) return OptionalDouble.empty();
        try {
            double similarity = cosine(embed(text), classification.equals("TECHNICAL") ? technicalAnchor : professionalAnchor);
            return OptionalDouble.of(Math.max(0.0, Math.min(1.0, (similarity + 1d) / 2d)));
        } catch (Exception exception) {
            log.debug("ONNX sentence inference failed; retaining fallback relevance", exception);
            return OptionalDouble.empty();
        }
    }

    private double[] embed(String text) throws OrtException {
        long[][] ids = new long[1][MAX_TOKENS]; long[][] mask = new long[1][MAX_TOKENS]; long[][] types = new long[1][MAX_TOKENS];
        Arrays.fill(ids[0], vocabulary.getOrDefault("[PAD]", 0L));
        List<Long> sequence = encode(text); int count = Math.min(MAX_TOKENS, sequence.size());
        for (int i = 0; i < count; i++) { ids[0][i] = sequence.get(i); mask[0][i] = 1L; }
        Map<String, OnnxTensor> input = new HashMap<>();
        input.put("input_ids", OnnxTensor.createTensor(environment, ids));
        input.put("attention_mask", OnnxTensor.createTensor(environment, mask));
        if (session.getInputNames().contains("token_type_ids")) input.put("token_type_ids", OnnxTensor.createTensor(environment, types));
        try (OrtSession.Result result = session.run(input)) {
            OnnxValue output = result.get(0);
            float[][][] hidden = (float[][][]) ((OnnxTensor) output).getValue();
            double[] pooled = new double[hidden[0][0].length];
            for (int token = 0; token < count; token++) for (int dimension = 0; dimension < pooled.length; dimension++) pooled[dimension] += hidden[0][token][dimension];
            for (int dimension = 0; dimension < pooled.length; dimension++) pooled[dimension] /= Math.max(1, count);
            return pooled;
        } finally { for (OnnxTensor tensor : input.values()) tensor.close(); }
    }

    private List<Long> encode(String text) {
        List<Long> ids = new ArrayList<>(); ids.add(vocabulary.getOrDefault("[CLS]", 101L));
        Matcher matcher = TOKENS.matcher(text.toLowerCase(Locale.ROOT));
        while (matcher.find() && ids.size() < MAX_TOKENS - 1) {
            for (String token : wordPieces(matcher.group())) { ids.add(vocabulary.getOrDefault(token, vocabulary.getOrDefault("[UNK]", 100L))); if (ids.size() >= MAX_TOKENS - 1) break; }
        }
        ids.add(vocabulary.getOrDefault("[SEP]", 102L)); return ids;
    }
    private List<String> wordPieces(String token) {
        if (vocabulary.containsKey(token)) return List.of(token);
        List<String> pieces = new ArrayList<>(); int start = 0;
        while (start < token.length()) {
            int end = token.length(); String piece = null;
            while (start < end) { String candidate = (start == 0 ? "" : "##") + token.substring(start, end); if (vocabulary.containsKey(candidate)) { piece = candidate; break; } end--; }
            if (piece == null) return List.of("[UNK]"); pieces.add(piece); start = end;
        }
        return pieces;
    }
    private Map<String, Long> loadVocabulary(Path path) throws IOException { Map<String, Long> result = new HashMap<>(); List<String> entries = Files.readAllLines(path); for (int index = 0; index < entries.size(); index++) result.put(entries.get(index).trim(), (long) index); return result; }
    private double cosine(double[] a, double[] b) { double dot = 0, aNorm = 0, bNorm = 0; for (int i = 0; i < a.length; i++) { dot += a[i] * b[i]; aNorm += a[i] * a[i]; bNorm += b[i] * b[i]; } return dot / Math.max(1e-9, Math.sqrt(aNorm) * Math.sqrt(bNorm)); }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    @PreDestroy void closeSession() { if (session != null) { try { session.close(); } catch (OrtException ignored) { } session = null; } }
}
