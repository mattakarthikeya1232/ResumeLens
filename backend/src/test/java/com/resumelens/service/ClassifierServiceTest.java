package com.resumelens.service;

import com.resumelens.config.ResumeLensProperties;
import com.resumelens.parser.SectionDetector.DetectedSection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClassifierServiceTest {
    private final ResumeLensProperties configuration = new ResumeLensProperties(8_388_608, 0.62, 20,
            new ResumeLensProperties.Embedding("", "", "all-MiniLM-L6-v2"),
            new ResumeLensProperties.Llm(false, "", "Qwen3-0.6B Q4"));
    private final ClassifierService service = new ClassifierService(new SkillCatalog(), configuration, new OnnxEmbeddingService(configuration));

    @Test
    void classifiesTechnicalAndProfessionalEvidenceWithSourceText() {
        var result = service.classify(List.of(
                new DetectedSection("Projects", "Developed Spring Boot services using Java and PostgreSQL."),
                new DetectedSection("Leadership", "Led a five-member team and presented the product demo.")
        ));

        assertThat(result.technicalSkills()).extracting(item -> item.skill()).contains("Java", "Spring Boot", "PostgreSQL");
        assertThat(result.technicalSkills()).extracting(item -> item.skill()).doesNotContain("Spring");
        assertThat(result.nonTechnicalSkills()).extracting(item -> item.skill()).contains("Leadership", "Presentation");
        assertThat(result.evidence()).allSatisfy(item -> assertThat(item.text()).isNotBlank());
        assertThat(result.technicalScore() + result.nonTechnicalScore()).isEqualTo(100);
    }

    @Test
    void reportsNoScoresWhenThereIsNoSupportedEvidence() {
        var result = service.classify(List.of(new DetectedSection("Profile", "Seeking an opportunity to learn and contribute.")));
        assertThat(result.technicalSkills()).isEmpty();
        assertThat(result.nonTechnicalSkills()).isEmpty();
        assertThat(result.technicalScore()).isZero();
        assertThat(result.nonTechnicalScore()).isZero();
    }
}
