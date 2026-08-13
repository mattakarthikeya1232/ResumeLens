package com.resumelens.dto;

import java.util.List;

public record SkillMatch(
        String skill,
        String category,
        List<String> evidence,
        double relevance,
        String sourceSection
) { }
