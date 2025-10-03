package com.doc.pdfgen;

import com.doc.pdfgen.dto.MetaData;
import com.doc.pdfgen.pdf.service.MetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MetadataServiceIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(MetadataServiceIntegrationTest.class);

    @Autowired
    private MetadataService metadataService;

    private MockMultipartFile pdfFile;
    private MockMultipartFile docFile;
    private MockMultipartFile imageFile;
    private MockMultipartFile textFile;

    @BeforeEach
    void setUp() throws IOException {
        // Load test files from resources
        pdfFile = createMultipartFileFromResource(
                "test.pdf",
                "application/pdf",
                "test-files/test.pdf"
        );
        
        docFile = createMultipartFileFromResource(
                "test.doc",
                "application/msword",
                "test-files/test.doc"
        );
        
        imageFile = createMultipartFileFromResource(
                "test.png",
                "image/png",
                "test-files/test.png"
        );
        
        textFile = createMultipartFileFromResource(
                "test.txt",
                "text/plain",
                "test-files/test.txt"
        );
    }

    @Test
    void extractMetadata_PDF_ShouldExtractCorrectMetadata() throws IOException {
        // When
        MetaData metadata = metadataService.extractFileMetadata(pdfFile);

        logMetaData(metadata);
        assertNotNull(metadata);
        assertEquals("PDF", metadata.getFileType());
        assertNotNull(metadata.getPageCount());
        assertTrue(metadata.getPageCount() > 0);
        assertNotNull(metadata.getPageSize());
        assertNotNull(metadata.getOrientation());
    }

    private static void logMetaData(MetaData metadata) {
        logger.info("Metadata: {}", metadata);
    }

    @Test
    void extractMetadata_DOC_ShouldExtractCorrectMetadata() throws IOException {
        // When
        MetaData metadata = metadataService.extractFileMetadata(docFile);

        logMetaData(metadata);
        assertNotNull(metadata);
        assertEquals("application/x-tika-msoffice", metadata.getFileType());
        assertNotNull(metadata.getTitle());
        assertNotNull(metadata.getCreationDate());
    }

    @Test
    void extractMetadata_Image_ShouldExtractCorrectMetadata() throws IOException {
        // When
        MetaData metadata = metadataService.extractFileMetadata(imageFile);

        logMetaData(metadata);
        assertNotNull(metadata);
        assertTrue(metadata.getFileType().startsWith("image/"));
        assertNotNull(metadata.getPageSize()); // Should contain image dimensions
    }

    @Test
    void extractMetadata_UnsupportedFormat_ShouldReturnBasicMetadata() throws IOException {
        // When
        MetaData metadata = metadataService.extractFileMetadata(textFile);

        logMetaData(metadata);
        assertNotNull(metadata);
        assertEquals("text/plain", metadata.getFileType());
        assertEquals(textFile.getOriginalFilename(), metadata.getTitle());
        assertEquals(textFile.getSize(), metadata.getFileSize());
        assertNotNull(metadata.getCreationDate());
    }

    @Test
    void extractMetadata_NullFile_ShouldThrowException() {
        // Then
        assertThrows(NullPointerException.class, () -> {
            metadataService.extractFileMetadata(null);
        });
    }

    private MockMultipartFile createMultipartFileFromResource(String filename, String contentType, String resourcePath)
            throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (InputStream inputStream = resource.getInputStream()) {
            return new MockMultipartFile(
                    "file",
                    filename,
                    contentType,
                    inputStream.readAllBytes()
            );
        }
    }
}
