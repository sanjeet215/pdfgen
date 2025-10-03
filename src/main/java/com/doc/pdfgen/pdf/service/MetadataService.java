package com.doc.pdfgen.pdf.service;

import com.doc.pdfgen.dto.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Qualifier("metadataService")
@Service
public class MetadataService extends AbstractMetaDataExtractor {
    private final Logger logger = LoggerFactory.getLogger(MetadataService.class);

    @Autowired
    public MetadataService(
            @Qualifier("pdfExtractor") FileTypeMetadataExtractor pdfExtractor,
            @Qualifier("imageExtractor") FileTypeMetadataExtractor imageExtractor,
            @Qualifier("docExtractor") FileTypeMetadataExtractor docExtractor,
            @Qualifier("docxExtractor") FileTypeMetadataExtractor docxExtractor) {
        super();
    }

    public MetaData extractFileMetadata(MultipartFile file) {
        logger.debug("Extracting metadata for file: {}", file.getOriginalFilename());
        return extractMetaData(file);
    }
}
