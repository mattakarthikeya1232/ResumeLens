package com.resumelens.parser;

import com.resumelens.exception.ApiException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class DocxTextExtractor implements DocumentTextExtractor {
    @Override public boolean supports(String filename) { return filename.toLowerCase().endsWith(".docx"); }
    @Override public String extract(byte[] contents) {
        try (var document = new XWPFDocument(new ByteArrayInputStream(contents))) {
            return String.join("\n", document.getParagraphs().stream().map(paragraph -> paragraph.getText().trim()).toList());
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Unable to extract text from this DOCX file. Please upload a valid Word document.");
        }
    }
}
