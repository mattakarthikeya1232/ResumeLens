package com.resumelens.model;

import com.resumelens.dto.EvidenceItem;
import com.resumelens.dto.SkillMatch;

import java.util.List;

public record ClassificationOutput(List<SkillMatch> technicalSkills, List<SkillMatch> nonTechnicalSkills,
                                   List<EvidenceItem> evidence, int technicalScore, int nonTechnicalScore,
                                   long inferenceTimeMs, String mode) { }
