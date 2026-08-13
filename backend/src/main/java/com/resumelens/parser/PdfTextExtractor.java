package com.resumelens.parser;

import com.resumelens.exception.ApiException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class PdfTextExtractor implements DocumentTextExtractor {
    @Override public boolean supports(String filename) { return filename.toLowerCase().endsWith(".pdf"); }
    @Override public String extract(byte[] contents) {
        try (var document = Loader.loadPDF(contents)) {
            return new PDFTextStripper().getText(document);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Unable to extract text from this PDF. It may be image-only, encrypted, or malformed.");
        }
    }
}
