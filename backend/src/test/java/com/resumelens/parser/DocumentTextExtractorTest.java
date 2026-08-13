package com.resumelens.parser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTextExtractorTest {
    @Test
    void extractsTextFromPdf() throws Exception {
        byte[] bytes;
        try (var document = new PDDocument(); var output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            try (var content = new PDPageContentStream(document, document.getPage(0))) {
                content.beginText(); content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12); content.newLineAtOffset(80, 700); content.showText("Developed Java services"); content.endText();
            }
            document.save(output); bytes = output.toByteArray();
        }
        assertThat(new PdfTextExtractor().extract(bytes)).contains("Developed Java services");
    }

    @Test
    void extractsTextFromDocx() throws Exception {
        byte[] bytes;
        try (var document = new XWPFDocument(); var output = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText("Collaborated with a product team"); document.write(output); bytes = output.toByteArray();
        }
        assertThat(new DocxTextExtractor().extract(bytes)).contains("Collaborated with a product team");
    }
}
