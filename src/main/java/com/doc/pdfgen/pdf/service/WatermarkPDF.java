package com.doc.pdfgen.pdf.service;

import com.doc.pdfgen.dto.PDFContext;
import com.doc.pdfgen.dto.WaterMarkProp;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
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
        byte[] byteArray = pdfContext.getPdfBytes();
        try (PDDocument document = PDDocument.load(byteArray);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            String watermarkText = waterMarkProp.getWatermarkText() != null
                    ? waterMarkProp.getWatermarkText() : "WATERMARK";
            float fontSize = waterMarkProp.getWatermarkFontSize() != null
                    ? waterMarkProp.getWatermarkFontSize() : 48;
            float angle = waterMarkProp.getWatermarkAngle() != null
                    ? waterMarkProp.getWatermarkAngle() : -35;
            float opacity;
            try {
                opacity = Float.parseFloat(waterMarkProp.getWatermarkOpacity());
            } catch (Exception ignored) {
                opacity = 0.25f;
            }
            opacity = Math.max(0.05f, Math.min(1f, opacity));
            int[] color = parseColor(waterMarkProp.getWatermarkColor());

            for (PDPage page : document.getPages()) {
                PDRectangle pageSize = page.getMediaBox();
                float stringWidth = PDType1Font.HELVETICA_BOLD
                        .getStringWidth(watermarkText) / 1000 * fontSize;
                float[] point = position(waterMarkProp.getWatermarkPosition(),
                        pageSize, stringWidth, fontSize);
                try (PDPageContentStream contentStream = new PDPageContentStream(
                        document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    PDExtendedGraphicsState graphics = new PDExtendedGraphicsState();
                    graphics.setNonStrokingAlphaConstant(opacity);
                    graphics.setStrokingAlphaConstant(opacity);
                    contentStream.setGraphicsStateParameters(graphics);
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
                    contentStream.setNonStrokingColor(color[0], color[1], color[2]);
                    contentStream.setRenderingMode(RenderingMode.FILL);
                    double radians = Math.toRadians(angle);
                    contentStream.setTextMatrix(
                            (float) Math.cos(radians), (float) Math.sin(radians),
                            (float) -Math.sin(radians), (float) Math.cos(radians),
                            point[0], point[1]);
                    contentStream.showText(watermarkText);
                    contentStream.endText();
                }
            }
            document.save(baos);
            pdfContext.setPdfBytes(baos.toByteArray());
        }
    }

    private static float[] position(String position, PDRectangle page, float textWidth, float fontSize) {
        float padding = 36;
        String value = position == null ? "center" : position;
        float x = switch (value) {
            case "top-left", "bottom-left" -> padding;
            case "top-right", "bottom-right" -> Math.max(padding, page.getWidth() - textWidth - padding);
            default -> (page.getWidth() - textWidth) / 2;
        };
        float y = switch (value) {
            case "top-left", "top-center", "top-right" -> page.getHeight() - fontSize - padding;
            case "bottom-left", "bottom-center", "bottom-right" -> padding;
            default -> (page.getHeight() - fontSize) / 2;
        };
        return new float[]{x, y};
    }

    private static int[] parseColor(String value) {
        String hex = value == null ? "#6B7280" : value.trim();
        if (!hex.matches("#[0-9a-fA-F]{6}")) hex = "#6B7280";
        return new int[]{
                Integer.parseInt(hex.substring(1, 3), 16),
                Integer.parseInt(hex.substring(3, 5), 16),
                Integer.parseInt(hex.substring(5, 7), 16)
        };
    }
}
