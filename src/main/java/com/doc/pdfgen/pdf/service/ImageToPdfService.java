package com.doc.pdfgen.pdf.service;

import com.doc.pdfgen.common.CommonUtils;
import com.doc.pdfgen.dto.PDFContext;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
@Order(3)
public class ImageToPdfService implements PDFProcessService {

    private static final Logger logger = LoggerFactory.getLogger(ImageToPdfService.class);

    @Override
    public void processPDF(PDFContext context) {
        try {
            byte[] imageBytes = context.getInputFile().getBytes();

            try (PDDocument pdDocument = new PDDocument()) {
                PDRectangle pdRectangle = CommonUtils.getPDRectangle(context.getPdfOperationRequestDTO().getPageSize());
                PDPage page = new PDPage(pdRectangle);
                pdDocument.addPage(page);

                try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
                    BufferedImage bufferedImage = ImageIO.read(bais);
                    if (bufferedImage == null) {
                        throw new IOException("Invalid image format or unable to read image");
                    }

                    PDImageXObject pdImageXObject = LosslessFactory.createFromImage(pdDocument, bufferedImage);

                    float pageWidth = page.getMediaBox().getWidth();
                    float pageHeight = page.getMediaBox().getHeight();
                    float imageWidth = bufferedImage.getWidth();
                    float imageHeight = bufferedImage.getHeight();

                    float scaleX = pageWidth / imageWidth;
                    float scaleY = pageHeight / imageHeight;
                    float scale = Math.min(scaleX, scaleY);

                    float xOffset = (pageWidth - (imageWidth * scale)) / 2;
                    float yOffset = (pageHeight - (imageHeight * scale)) / 2;

                    try (PDPageContentStream contentStream = new PDPageContentStream(pdDocument, page)) {
                        switch (context.getPdfOperationRequestDTO().getBorderType()) {
                            case THIN ->
                                    drawBorder(contentStream, xOffset, yOffset, imageWidth * scale, imageHeight * scale, 5);
                            case THICK ->
                                    drawBorder(contentStream, xOffset, yOffset, imageWidth * scale, imageHeight * scale, 25);
                            case NO_BORDER -> { /* do nothing */ }
                        }

                        contentStream.drawImage(pdImageXObject, xOffset, yOffset, imageWidth * scale, imageHeight * scale);
                    }

                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        pdDocument.save(baos);
                        context.setPdfBytes(baos.toByteArray());
                    }
                }
            }

        } catch (IOException ioException) {
            throw new RuntimeException("Some IOException occurred", ioException);
        }
    }

    private void drawBorder(PDPageContentStream contentStream, float xOffset, float yOffset, float width, float height, float borderWidth) throws IOException {
        contentStream.setStrokingColor(Color.BLACK); // better default
        contentStream.setLineWidth(borderWidth);
        contentStream.addRect(xOffset - borderWidth / 2, yOffset - borderWidth / 2, width + borderWidth, height + borderWidth);
        contentStream.stroke();
    }
}
