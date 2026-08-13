package com.resumelens.service;

import com.resumelens.model.SkillDefinition;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SkillCatalog {
    public List<SkillDefinition> all() {
        return List.of(
                tech("Java", "Programming Languages", "java"), tech("Python", "Programming Languages", "python"), tech("C++", "Programming Languages", "c\\+\\+"),
                tech("C#", "Programming Languages", "c#"), tech("JavaScript", "Programming Languages", "javascript"), tech("TypeScript", "Programming Languages", "typescript"),
                tech("Kotlin", "Programming Languages", "kotlin"), tech("Go", "Programming Languages", "golang|\\bgo\\b"), tech("Rust", "Programming Languages", "rust"),
                tech("Spring Boot", "Frameworks", "spring boot"), tech("Spring", "Frameworks", "spring"), tech("React", "Frameworks", "react"), tech("Angular", "Frameworks", "angular"),
                tech("Django", "Frameworks", "django"), tech("Node.js", "Frameworks", "node\\.?js"), tech("Express", "Frameworks", "express"), tech("Android", "Frameworks", "android"),
                tech("MySQL", "Databases", "mysql"), tech("PostgreSQL", "Databases", "postgresql|postgres"), tech("MongoDB", "Databases", "mongodb|mongo db"),
                tech("Redis", "Databases", "redis"), tech("Firebase", "Databases", "firebase"), tech("SQLite", "Databases", "sqlite"),
                tech("AWS", "Cloud / DevOps", "aws|amazon web services"), tech("Azure", "Cloud / DevOps", "azure"), tech("GCP", "Cloud / DevOps", "gcp|google cloud"),
                tech("Docker", "Cloud / DevOps", "docker"), tech("Kubernetes", "Cloud / DevOps", "kubernetes|k8s"), tech("GitHub Actions", "Cloud / DevOps", "github actions"),
                tech("Git", "Cloud / DevOps", "git"), tech("CI/CD", "Cloud / DevOps", "ci/cd|continuous integration|continuous delivery"),
                tech("Machine Learning", "AI / ML", "machine learning"), tech("Deep Learning", "AI / ML", "deep learning"), tech("NLP", "AI / ML", "natural language processing|\\bnlp\\b"),
                tech("TensorFlow", "AI / ML", "tensorflow"), tech("PyTorch", "AI / ML", "pytorch"), tech("Scikit-learn", "AI / ML", "scikit[ -]?learn"),
                soft("Communication", "communication|communicated|stakeholder"), soft("Leadership", "leadership|led|lead a team"), soft("Teamwork", "teamwork|team player"),
                soft("Collaboration", "collaboration|collaborated|cross-functional"), soft("Problem Solving", "problem[ -]?solving|solved complex|resolved"),
                soft("Management", "management|managed|mentored"), soft("Presentation", "presentation|presented|demoed"), soft("Public Speaking", "public speaking|speaker"),
                soft("Time Management", "time management|prioritized|deadline"), soft("Adaptability", "adaptab|adapted"), soft("Creativity", "creativ|innovative"),
                soft("Organization", "organiz|coordinated|planned")
        );
    }
    private SkillDefinition tech(String name, String category, String pattern) { return new SkillDefinition(name, category, "TECHNICAL", pattern); }
    private SkillDefinition soft(String name, String pattern) { return new SkillDefinition(name, "Professional Skills", "NON_TECHNICAL", pattern); }
}
