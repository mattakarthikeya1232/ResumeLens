package com.resumelens.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SectionDetectorTest {
    @Test
    void detectsCommonHeadingsAndKeepsTheirContent() {
        var sections = new SectionDetector().detect("SUMMARY\nJava developer\n\nTECHNICAL SKILLS\nJava, Spring Boot\n\nPROJECTS\nBuilt an API.");
        assertThat(sections).extracting(SectionDetector.DetectedSection::name).containsExactly("Summary", "Technical Skills", "Projects");
        assertThat(sections.get(1).text()).contains("Spring Boot");
    }
}
