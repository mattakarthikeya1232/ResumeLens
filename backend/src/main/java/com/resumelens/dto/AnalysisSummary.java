package com.resumelens.dto;

import java.time.Instant;

public record AnalysisSummary(String analysisId, String resumeName, Instant analyzedAt, int technicalScore,
                              int nonTechnicalScore, String status) { }
