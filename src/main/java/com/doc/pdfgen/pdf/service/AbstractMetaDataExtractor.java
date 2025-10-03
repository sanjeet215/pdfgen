package com.doc.pdfgen.pdf.service;

import com.doc.pdfgen.dto.MetaData;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

abstract class AbstractMetaDataExtractor {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMetaDataExtractor.class);
    private final Map<String, FileTypeMetadataExtractor> extractors;

    protected AbstractMetaDataExtractor() {
        this.extractors = new HashMap<>();
        initializeExtractors();
    }

    private void initializeExtractors() {
        extractors.put("application/pdf", new PdfMetadataExtractor());
        extractors.put("image/", new ImageMetadataExtractor());
        extractors.put("application/msword", new DocMetadataExtractor());
        extractors.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", new DocxMetadataExtractor());
    }

    protected MetaData extractMetaData(MultipartFile multipartFile) {
        logger.debug(">>extractMetaData");
        MetaData metaData = new MetaData();

        try {
            // Detect file type using Tika
            Tika tika = new Tika();
            String detectedMimeType = tika.detect(multipartFile.getInputStream());
            metaData.setFileType(detectedMimeType);
            metaData.setFileSize(multipartFile.getSize());

            // Find appropriate extractor
            FileTypeMetadataExtractor extractor = findExtractor(detectedMimeType);
            if (extractor != null) {
                extractor.extractMetadata(multipartFile, metaData);
            } else {
                // Fall back to basic metadata for unknown file types
                extractBasicMetadata(multipartFile, metaData);
            }

        } catch (IOException e) {
            logger.error("Error extracting metadata: {}", e.getMessage());
            throw new RuntimeException("Failed to extract metadata from file", e);
        }

        logger.debug("<<extractMetaData");
        return metaData;
    }

    private FileTypeMetadataExtractor findExtractor(String mimeType) {
        return extractors.entrySet().stream()
                .filter(entry -> mimeType.startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private void extractBasicMetadata(MultipartFile file, MetaData metaData) {
        metaData.setTitle(file.getOriginalFilename());
        metaData.setCreationDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date()));
    }
}


