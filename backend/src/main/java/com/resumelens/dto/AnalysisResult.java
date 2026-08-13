package com.resumelens.dto;

import java.time.Instant;
import java.util.List;

public record AnalysisResult(
        String analysisId,
        String resumeName,
        Instant analyzedAt,
        String status,
        int technicalScore,
        int nonTechnicalScore,
        List<SkillMatch> technicalSkills,
        List<SkillMatch> nonTechnicalSkills,
        List<ResumeSection> sections,
        List<EvidenceItem> evidence,
        Explanation explanation,
        Diagnostics diagnostics,
        String processingMode
) { }
