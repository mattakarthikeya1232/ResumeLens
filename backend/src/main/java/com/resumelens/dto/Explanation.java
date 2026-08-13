package com.resumelens.dto;

import java.util.List;

public record Explanation(
        String summary,
        String technicalProfile,
        String nonTechnicalProfile,
        List<String> strengths,
        List<String> improvementAreas,
        String candidateProfile,
        String generatedBy
) { }
