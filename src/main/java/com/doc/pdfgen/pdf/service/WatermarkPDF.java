package com.doc.pdfgen.pdf.service;

import com.doc.pdfgen.dto.PDFContext;
import com.doc.pdfgen.dto.WaterMarkProp;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
@Order(4)
public class WatermarkPDF implements PDFProcessService {

    private static final Logger logger = LoggerFactory.getLogger(WatermarkPDF.class);

    @Override
    public void processPDF(PDFContext pdfContext) {
        logger.debug(">>watermarkPdf");
        if (!pdfContext.getRequestTypeDTO().isWatermarkRequired()) {
            return;
        }
        WaterMarkProp waterMarkProp = pdfContext.getRequestTypeDTO().getWaterMarkProp();
        if (waterMarkProp != null) {
            watermarkThePdfWithText(pdfContext, waterMarkProp);
        }
        logger.debug("<<watermarkPdf");
    }

    private void watermarkThePdfWithText(PDFContext pdfContext, WaterMarkProp waterMarkProp) {
        logger.debug(">>watermarkThePdfWithText");
        try {
            writeWaterMarkTextOnPDF(pdfContext, waterMarkProp);
        } catch (IOException ioException) {
            throw new RuntimeException("Some IOException occurred", ioException);
        }

        logger.debug("<<watermarkThePdfWithText");
    }

    private static void writeWaterMarkTextOnPDF(PDFContext pdfContext, WaterMarkProp waterMarkProp) throws IOException {
        byte[] byteArray = pdfContext.getInputFile().getBytes();
        PDDocument document = PDDocument.load(byteArray);

        String watermarkText = waterMarkProp.getWatermarkText() != null ? waterMarkProp.getWatermarkText() : "Default Watermark";
        float fontSize = waterMarkProp.getWatermarkFontSize() != null ? waterMarkProp.getWatermarkFontSize() : 60;
        float angle = waterMarkProp.getWatermarkAngle() != null ? waterMarkProp.getWatermarkAngle() : 45;

        for (PDPage page : document.getPages()) {
            PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);

            PDRectangle pageSize = page.getMediaBox();
            float stringWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(watermarkText) / 1000 * fontSize;

            // Center point
            float x = (pageSize.getWidth() - stringWidth) / 2;
            float y = (pageSize.getHeight() / 2) - (fontSize / 4);

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
            contentStream.setNonStrokingColor(200, 200, 200); // light gray color
            contentStream.setRenderingMode(RenderingMode.FILL_STROKE);

            // Rotate text around center
            contentStream.setTextMatrix(
                    (float) Math.cos(Math.toRadians(angle)),
                    (float) Math.sin(Math.toRadians(angle)),
                    (float) -Math.sin(Math.toRadians(angle)),
                    (float) Math.cos(Math.toRadians(angle)),
                    x,
                    y);
            contentStream.showText(watermarkText);
            contentStream.endText();
            contentStream.close();
        }

        // Update the PDFContext with the modified document
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        pdfContext.setPdfBytes(baos.toByteArray());
        document.close();
    }
}