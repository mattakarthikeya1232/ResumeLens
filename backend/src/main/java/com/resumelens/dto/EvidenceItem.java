package com.resumelens.dto;

public record EvidenceItem(
        String classification,
        String skill,
        String category,
        String sourceSection,
        String text,
        double relevance
) { }
