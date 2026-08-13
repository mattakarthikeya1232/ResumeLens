package com.resumelens.controller;

import com.resumelens.config.ResumeLensProperties;
import com.resumelens.exception.ApiExceptionHandler;
import com.resumelens.llm.DeterministicExplanationService;
import com.resumelens.llm.LocalLlmService;
import com.resumelens.parser.DocxTextExtractor;
import com.resumelens.parser.PdfTextExtractor;
import com.resumelens.parser.SectionDetector;
import com.resumelens.service.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResumeControllerTest {
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        var configuration = new ResumeLensProperties(8_388_608, 0.62, 20,
                new ResumeLensProperties.Embedding("", "", "all-MiniLM-L6-v2"),
                new ResumeLensProperties.Llm(false, "", "Qwen3-0.6B Q4"));
        var embedding = new OnnxEmbeddingService(configuration);
        var localLlm = new LocalLlmService(configuration);
        var analysis = new ResumeAnalysisService(List.of(new PdfTextExtractor(), new DocxTextExtractor()), new SectionDetector(),
                new ClassifierService(new SkillCatalog(), configuration, embedding), new DeterministicExplanationService(), localLlm,
                new DiagnosticsService(configuration, localLlm, embedding), configuration);
        mvc = MockMvcBuilders.standaloneSetup(new ResumeController(analysis, new DiagnosticsService(configuration, localLlm, embedding), localLlm))
                .setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @Test
    void acceptsDocxAndReturnsEvidenceBackedAnalysis() throws Exception {
        var file = new MockMultipartFile("file", "candidate.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", validDocx());

        mvc.perform(multipart("/api/resumes/analyze").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.technicalScore").isNumber())
                .andExpect(jsonPath("$.technicalSkills[0].evidence[0]").isNotEmpty())
                .andExpect(jsonPath("$.explanation.generatedBy").value("Deterministic local fallback"));
    }

    @Test
    void rejectsRenamedNonPdfBeforeParsing() throws Exception {
        var file = new MockMultipartFile("file", "not-a-resume.pdf", "application/pdf", "not actually a pdf".getBytes());

        mvc.perform(multipart("/api/resumes/analyze").file(file))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value("The file content does not match its PDF or DOCX extension."));
    }

    private byte[] validDocx() throws Exception {
        try (var document = new XWPFDocument(); var output = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText("SKILLS");
            document.createParagraph().createRun().setText("Java, Spring Boot, PostgreSQL");
            document.createParagraph().createRun().setText("EXPERIENCE");
            document.createParagraph().createRun().setText("Developed Java services and collaborated with a product team.");
            document.write(output);
            return output.toByteArray();
        }
    }
}
