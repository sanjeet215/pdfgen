package com.doc.pdfgen.pdf.service;

import com.doc.pdfgen.dto.PDFContext;
import com.doc.pdfgen.dto.PageNumberDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
@Order(4)
public class PageNumberService implements PDFProcessService {

    @Override
    public void processPDF(PDFContext context) {
        if (!context.getRequestTypeDTO().isPageNumberPDF()) {
            return;
        }
        PageNumberDTO options = context.getRequestTypeDTO().getPageNumberDTO();
        if (options == null) {
            throw new IllegalArgumentException("Page number options are required");
        }
        try {
            context.setPdfBytes(addPageNumbers(context.getPdfBytes(), options));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to add page numbers", exception);
        }
    }

    private byte[] addPageNumbers(byte[] input, PageNumberDTO options) throws IOException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(input));
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            int pageNumber = options.getStartNumber();
            for (PDPage page : document.getPages()) {
                String label = String.valueOf(pageNumber++);
                float fontSize = options.getFontSize();
                float textWidth = PDType1Font.HELVETICA.getStringWidth(label) / 1000 * fontSize;
                float margin = 28;
                float x;
                float y;
                String position = options.getPosition() == null ? "bottom-center" : options.getPosition();
                switch (position) {
                    case "bottom-left" -> {
                        x = margin;
                        y = margin;
                    }
                    case "bottom-right" -> {
                        x = page.getMediaBox().getWidth() - margin - textWidth;
                        y = margin;
                    }
                    case "top-left" -> {
                        x = margin;
                        y = page.getMediaBox().getHeight() - margin - fontSize;
                    }
                    case "top-center" -> {
                        x = (page.getMediaBox().getWidth() - textWidth) / 2;
                        y = page.getMediaBox().getHeight() - margin - fontSize;
                    }
                    case "top-right" -> {
                        x = page.getMediaBox().getWidth() - margin - textWidth;
                        y = page.getMediaBox().getHeight() - margin - fontSize;
                    }
                    default -> {
                        x = (page.getMediaBox().getWidth() - textWidth) / 2;
                        y = margin;
                    }
                }

                try (PDPageContentStream stream = new PDPageContentStream(
                        document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    stream.beginText();
                    stream.setFont(PDType1Font.HELVETICA, fontSize);
                    stream.setNonStrokingColor(60, 60, 60);
                    stream.newLineAtOffset(x, y);
                    stream.showText(label);
                    stream.endText();
                }
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
