package com.resumelens.parser;

import com.resumelens.dto.ResumeSection;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class SectionDetector {
    private static final Map<String, String> HEADINGS = Map.ofEntries(
            Map.entry("summary", "Summary"), Map.entry("profile", "Summary"), Map.entry("objective", "Objective"),
            Map.entry("education", "Education"), Map.entry("skills", "Skills"), Map.entry("technical skills", "Technical Skills"),
            Map.entry("experience", "Experience"), Map.entry("work experience", "Experience"), Map.entry("projects", "Projects"),
            Map.entry("certifications", "Certifications"), Map.entry("achievements", "Achievements"), Map.entry("leadership", "Leadership"),
            Map.entry("activities", "Activities"), Map.entry("publications", "Publications"), Map.entry("interests", "Interests")
    );
    private static final Pattern CLEAN = Pattern.compile("[^a-z ]");

    public List<DetectedSection> detect(String text) {
        var sections = new ArrayList<DetectedSection>();
        var buffers = new LinkedHashMap<String, StringBuilder>();
        String active = "Profile";
        buffers.put(active, new StringBuilder());
        for (String rawLine : text.replace('\r', '\n').split("\\n")) {
            String line = rawLine.trim();
            if (line.isBlank()) continue;
            String normalized = CLEAN.matcher(line.toLowerCase(Locale.ROOT)).replaceAll("").replaceAll("\\s+", " ").trim();
            if (HEADINGS.containsKey(normalized) && line.length() < 42) {
                active = HEADINGS.get(normalized);
                buffers.putIfAbsent(active, new StringBuilder());
            } else {
                buffers.get(active).append(line).append('\n');
            }
        }
        buffers.forEach((name, body) -> {
            String content = body.toString().trim();
            if (!content.isBlank()) sections.add(new DetectedSection(name, content));
        });
        return sections.isEmpty() ? List.of(new DetectedSection("Profile", text.trim())) : sections;
    }

    public List<ResumeSection> publicSections(List<DetectedSection> sections) {
        return sections.stream().map(section -> new ResumeSection(section.name(), preview(section.text()), section.text().length())).toList();
    }

    private String preview(String text) { return text.length() <= 180 ? text : text.substring(0, 177) + "..."; }
    public record DetectedSection(String name, String text) { }
}
