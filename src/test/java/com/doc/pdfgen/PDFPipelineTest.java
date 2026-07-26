package com.doc.pdfgen;

import com.doc.pdfgen.dto.BorderType;
import com.doc.pdfgen.dto.ImageToPdfDTO;
import com.doc.pdfgen.dto.RequestTypeDTO;
import com.doc.pdfgen.pdf.service.BuildPDFContext;
import com.doc.pdfgen.pdf.service.ImageToPdfService;
import com.doc.pdfgen.service.PDFPipeline;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PDFPipelineTest {

    private final PDFPipeline pipeline = new PDFPipeline(
            List.of(new BuildPDFContext(), new ImageToPdfService()));

    @Test
    void mergesTwoImagesIntoAValidTwoPagePdf() throws Exception {
        RequestTypeDTO request = imageRequest(true, "portrait");

        byte[] result = pipeline.execute(
                List.of(image("first.png"), image("second.png")), request);

        try (PDDocument document = PDDocument.load(result)) {
            assertEquals(2, document.getNumberOfPages());
            assertEquals(595, Math.round(document.getPage(0).getMediaBox().getWidth()));
            assertEquals(842, Math.round(document.getPage(0).getMediaBox().getHeight()));
        }
    }

    @Test
    void appliesLandscapeOrientation() throws Exception {
        byte[] result = pipeline.execute(
                List.of(image("landscape.png")), imageRequest(false, "landscape"));

        try (PDDocument document = PDDocument.load(result)) {
            assertEquals(842, Math.round(document.getPage(0).getMediaBox().getWidth()));
            assertEquals(595, Math.round(document.getPage(0).getMediaBox().getHeight()));
        }
    }

    @Test
    void rejectsMultipleImagesWhenMergeIsDisabled() throws Exception {
        RequestTypeDTO request = imageRequest(false, "portrait");

        assertThrows(IllegalArgumentException.class, () ->
                pipeline.execute(List.of(image("first.png"), image("second.png")), request));
    }

    @Test
    void mergesExistingPdfFilesInProvidedOrder() throws Exception {
        RequestTypeDTO request = new RequestTypeDTO();
        request.setMergePDF(true);

        byte[] result = pipeline.execute(
                List.of(pdf("first.pdf", 1), pdf("second.pdf", 2)), request);

        try (PDDocument document = PDDocument.load(result)) {
            assertEquals(3, document.getNumberOfPages());
        }
    }

    private static RequestTypeDTO imageRequest(boolean mergeAll, String orientation) {
        ImageToPdfDTO options = new ImageToPdfDTO();
        options.setPageSize("A4");
        options.setOrientation(orientation);
        options.setBorderType(BorderType.NO_BORDER);
        options.setMergeAll(mergeAll);

        RequestTypeDTO request = new RequestTypeDTO();
        request.setImageToPdf(true);
        request.setImageToPdfDTO(options);
        return request;
    }

    private static MockMultipartFile image(String name) throws Exception {
        BufferedImage image = new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return new MockMultipartFile("files", name, "image/png", output.toByteArray());
        }
    }

    private static MockMultipartFile pdf(String name, int pages) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            for (int index = 0; index < pages; index++) {
                document.addPage(new org.apache.pdfbox.pdmodel.PDPage());
            }
            document.save(output);
            return new MockMultipartFile("files", name, "application/pdf", output.toByteArray());
        }
    }
}
