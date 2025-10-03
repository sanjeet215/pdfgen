package com.doc.pdfgen.pdf.service;

import com.doc.pdfgen.dto.MetaData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Component("pdfExtractor")
public class PdfMetadataExtractor implements FileTypeMetadataExtractor {

    private static final Logger logger = LoggerFactory.getLogger(PdfMetadataExtractor.class);

    @Override
    public void extractMetadata(MultipartFile multipartFile, MetaData metaData) throws IOException {
        logger.debug(">>extractMetaData");
        try (PDDocument document = PDDocument.load(multipartFile.getInputStream())) {
            PDDocumentInformation info = document.getDocumentInformation();

            // Basic metadata
            metaData.setTitle(info.getTitle());
            metaData.setAuthor(info.getAuthor());
            metaData.setSubject(info.getSubject());
            metaData.setKeywords(info.getKeywords());
            metaData.setCreator(info.getCreator());
            metaData.setProducer(info.getProducer());

            // Dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar creationDate = info.getCreationDate();
            Calendar modDate = info.getModificationDate();

            if (creationDate != null) {
                metaData.setCreationDate(dateFormat.format(creationDate.getTime()));
            }
            if (modDate != null) {
                metaData.setModificationDate(dateFormat.format(modDate.getTime()));
            }

            // Page information
            metaData.setPageCount(document.getNumberOfPages());

            // Get page size from the first page
            if (document.getNumberOfPages() > 0) {
                PDRectangle mediaBox = document.getPage(0).getMediaBox();
                float width = mediaBox.getWidth();
                float height = mediaBox.getHeight();

                // Determine page size and orientation
                String pageSize = determinePageSize(width, height);
                String orientation = (width > height) ? "Landscape" : "Portrait";

                metaData.setPageSize(pageSize);
                metaData.setOrientation(orientation);
            }

            // Security information
            metaData.setEncrypted(document.isEncrypted());

            // File information
            metaData.setFileType("PDF");
            metaData.setFileSize(multipartFile.getSize());

        } catch (IOException e) {
            logger.error("Error extracting metadata: {}", e.getMessage());
            throw new RuntimeException("Failed to extract metadata from PDF", e);
        }
    }

    private String determinePageSize(float width, float height) {
        // Convert points to millimeters (1 point = 0.3528 mm)
        float widthMM = width * 0.3528f;
        float heightMM = height * 0.3528f;

        // Common page sizes in mm
        if (isWithinTolerance(widthMM, 210) && isWithinTolerance(heightMM, 297)) {
            return "A4";
        } else if (isWithinTolerance(widthMM, 216) && isWithinTolerance(heightMM, 279)) {
            return "Letter";
        } else if (isWithinTolerance(widthMM, 216) && isWithinTolerance(heightMM, 356)) {
            return "Legal";
        } else if (isWithinTolerance(widthMM, 297) && isWithinTolerance(heightMM, 420)) {
            return "A3";
        } else if (isWithinTolerance(widthMM, 148) && isWithinTolerance(heightMM, 210)) {
            return "A5";
        }

        // If no standard size matches, return custom dimensions
        return String.format("Custom (%.0f x %.0f mm)", widthMM, heightMM);
    }

    private boolean isWithinTolerance(float value, float target) {
        float tolerance = 2.0f; // 2mm tolerance
        return Math.abs(value - target) <= tolerance;
    }


}
