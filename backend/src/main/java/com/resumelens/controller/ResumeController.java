package com.resumelens.controller;

import com.resumelens.dto.AnalysisResult;
import com.resumelens.dto.AnalysisSummary;
import com.resumelens.dto.Diagnostics;
import com.resumelens.dto.LocalLlmSettings;
import com.resumelens.dto.UpdateLocalLlmSettings;
import com.resumelens.llm.LocalLlmService;
import com.resumelens.service.DiagnosticsService;
import com.resumelens.service.ResumeAnalysisService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ResumeController {
    private final ResumeAnalysisService analyses;
    private final DiagnosticsService diagnostics;
    private final LocalLlmService localLlm;
    public ResumeController(ResumeAnalysisService analyses, DiagnosticsService diagnostics, LocalLlmService localLlm) {
        this.analyses = analyses; this.diagnostics = diagnostics; this.localLlm = localLlm;
    }

    @PostMapping(value = "/resumes/analyze", consumes = "multipart/form-data")
    public ResponseEntity<AnalysisResult> analyze(@RequestPart("file") MultipartFile file) { return ResponseEntity.status(201).body(analyses.analyze(file)); }
    @GetMapping("/analyses/{id}") public AnalysisResult analysis(@PathVariable @NotBlank String id) { return analyses.find(id); }
    @GetMapping("/analyses/{id}/skills") public Map<String, Object> skills(@PathVariable String id) { AnalysisResult value = analyses.find(id); return Map.of("technicalSkills", value.technicalSkills(), "nonTechnicalSkills", value.nonTechnicalSkills()); }
    @GetMapping("/analyses/{id}/evidence") public Map<String, Object> evidence(@PathVariable String id) { return Map.of("evidence", analyses.find(id).evidence()); }
    @GetMapping("/analyses/{id}/report") public AnalysisResult report(@PathVariable String id) { return analyses.find(id); }
    @GetMapping("/analyses") public List<AnalysisSummary> history() { return analyses.history(); }
    @DeleteMapping("/analyses/{id}") public ResponseEntity<Void> delete(@PathVariable String id) { analyses.delete(id); return ResponseEntity.noContent().build(); }
    @GetMapping("/health") public Map<String, String> health() { return Map.of("status", "UP", "service", "ResumeLens API"); }
    @GetMapping("/system/diagnostics") public Diagnostics systemDiagnostics() { return diagnostics.snapshot(0, 0, 0); }
    @GetMapping("/settings/local-llm") public LocalLlmSettings localLlmSettings() { return localLlm.settings(); }
    @PutMapping("/settings/local-llm") public LocalLlmSettings updateLocalLlmSettings(@RequestBody UpdateLocalLlmSettings request) { return localLlm.updateEnabled(request.enabled()); }
}
