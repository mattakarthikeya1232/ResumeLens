package com.resumelens.service;

import com.resumelens.config.ResumeLensProperties;
import com.resumelens.dto.EvidenceItem;
import com.resumelens.dto.SkillMatch;
import com.resumelens.model.ClassificationOutput;
import com.resumelens.model.SkillDefinition;
import com.resumelens.parser.SectionDetector.DetectedSection;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class ClassifierService {
    private final SkillCatalog catalog;
    private final ResumeLensProperties properties;
    private final OnnxEmbeddingService semanticModel;
    public ClassifierService(SkillCatalog catalog, ResumeLensProperties properties, OnnxEmbeddingService semanticModel) { this.catalog = catalog; this.properties = properties; this.semanticModel = semanticModel; }

    public ClassificationOutput classify(List<DetectedSection> sections) {
        long started = System.nanoTime();
        List<MatchedEvidence> matches = new ArrayList<>();
        for (DetectedSection section : sections) {
            for (String sentence : sentences(section.text())) {
                for (SkillDefinition definition : catalog.all()) {
                    if (Pattern.compile("(?i)(?<![a-z0-9])(" + definition.pattern() + ")(?![a-z0-9])").matcher(sentence).find()) {
                        double relevance = relevance(section.name(), sentence, definition.classification());
                        if (relevance >= properties.classificationThreshold()) matches.add(new MatchedEvidence(definition, section.name(), sentence.trim(), relevance));
                    }
                }
            }
        }
        List<MatchedEvidence> rawMatches = matches;
        matches = rawMatches.stream().filter(candidate -> rawMatches.stream().noneMatch(other -> isNestedSkill(candidate, other))).toList();
        List<SkillMatch> technical = skills(matches, "TECHNICAL");
        List<SkillMatch> nonTechnical = skills(matches, "NON_TECHNICAL");
        List<EvidenceItem> evidence = matches.stream().map(item -> new EvidenceItem(item.definition().classification(), item.definition().skill(), item.definition().category(), item.section(), item.text(), item.relevance())).toList();
        int[] scores = scores(matches);
        long elapsed = (System.nanoTime() - started) / 1_000_000;
        return new ClassificationOutput(technical, nonTechnical, evidence, scores[0], scores[1], elapsed, semanticModel.mode());
    }

    private List<SkillMatch> skills(List<MatchedEvidence> all, String classification) {
        return all.stream().filter(item -> item.definition().classification().equals(classification))
                .collect(java.util.stream.Collectors.groupingBy(item -> item.definition().skill(), LinkedHashMap::new, java.util.stream.Collectors.toList()))
                .values().stream().map(group -> {
                    MatchedEvidence first = group.getFirst();
                    List<String> evidence = group.stream().map(MatchedEvidence::text).distinct().limit(3).toList();
                    double relevance = group.stream().mapToDouble(MatchedEvidence::relevance).max().orElse(0);
                    return new SkillMatch(first.definition().skill(), first.definition().category(), evidence, relevance, first.section());
                }).sorted(Comparator.comparing(SkillMatch::category).thenComparing(SkillMatch::skill)).toList();
    }

    private int[] scores(List<MatchedEvidence> items) {
        double technical = items.stream().filter(item -> item.definition().classification().equals("TECHNICAL")).mapToDouble(MatchedEvidence::relevance).sum();
        double nonTechnical = items.stream().filter(item -> item.definition().classification().equals("NON_TECHNICAL")).mapToDouble(MatchedEvidence::relevance).sum();
        double total = technical + nonTechnical;
        if (total == 0) return new int[]{0, 0};
        return new int[]{(int) Math.round(technical / total * 100), (int) Math.round(nonTechnical / total * 100)};
    }

    private double relevance(String section, String sentence, String classification) {
        double score = 0.62;
        String lower = sentence.toLowerCase(Locale.ROOT);
        if (section.toLowerCase(Locale.ROOT).contains("skill")) score += 0.12;
        if (lower.matches(".*\\b(built|developed|implemented|led|managed|collaborated|designed|deployed|presented|created)\\b.*")) score += 0.12;
        if (sentence.length() > 35) score += 0.05;
        OptionalDouble semantic = semanticModel.relevance(sentence, classification);
        if (semantic.isPresent()) score = Math.max(score, 0.45 + semantic.getAsDouble() * 0.45);
        return Math.min(0.95, score);
    }

    private List<String> sentences(String text) {
        return Arrays.stream(text.replace('\n', ' ').split("(?<=[.!?;])\\s+|\\s+[•▪◦-]\\s+"))
                .filter(part -> !part.isBlank()).toList();
    }

    private boolean isNestedSkill(MatchedEvidence candidate, MatchedEvidence other) {
        if (candidate == other || !candidate.section().equals(other.section()) || !candidate.text().equals(other.text())) return false;
        if (!candidate.definition().classification().equals(other.definition().classification())) return false;
        String candidateSkill = candidate.definition().skill().toLowerCase(Locale.ROOT);
        String otherSkill = other.definition().skill().toLowerCase(Locale.ROOT);
        return otherSkill.length() > candidateSkill.length() && otherSkill.contains(candidateSkill);
    }

    private record MatchedEvidence(SkillDefinition definition, String section, String text, double relevance) { }
}
