package com.resumelens.llm;

import com.resumelens.dto.Explanation;
import com.resumelens.dto.SkillMatch;
import com.resumelens.model.ClassificationOutput;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeterministicExplanationService {
    public Explanation explain(ClassificationOutput analysis) {
        String technical = names(analysis.technicalSkills());
        String nonTechnical = names(analysis.nonTechnicalSkills());
        String technicalProfile = technical.isBlank() ? "Insufficient evidence in the resume." : "Evidence supports technical experience with " + technical + ".";
        String softProfile = nonTechnical.isBlank() ? "Insufficient evidence in the resume." : "Evidence supports professional strengths in " + nonTechnical + ".";
        String summary = "The classification is based on " + analysis.evidence().size() + " evidence signal" + (analysis.evidence().size() == 1 ? "" : "s") + ". " + technicalProfile + " " + softProfile;
        List<String> strengths = analysis.evidence().stream().sorted((a, b) -> Double.compare(b.relevance(), a.relevance()))
                .limit(4).map(item -> item.skill() + " — evidenced in " + item.sourceSection()).distinct().toList();
        List<String> improvements = List.of(
                analysis.nonTechnicalSkills().isEmpty() ? "Add evidence of collaboration, communication, or leadership where applicable." : "Quantify the impact of the professional skills already described.",
                analysis.technicalSkills().isEmpty() ? "Add concrete technologies, tools, or project outcomes where applicable." : "Pair technical skills with outcomes, scope, or measurable results."
        );
        String profile = analysis.technicalScore() == 0 && analysis.nonTechnicalScore() == 0 ? "Insufficient evidence in the resume." :
                (analysis.technicalScore() >= analysis.nonTechnicalScore() ? "Evidence-led technical profile with supporting professional signals." : "Evidence-led professional profile with supporting technical signals.");
        return new Explanation(summary, technicalProfile, softProfile, strengths, improvements, profile, "Deterministic local fallback");
    }

    private String names(List<SkillMatch> skills) {
        return skills.stream().map(SkillMatch::skill).limit(6).collect(Collectors.joining(", "));
    }
}
