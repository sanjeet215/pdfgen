package com.doc.pdfgen.pdf.service;

import com.doc.pdfgen.dto.MetaData;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public interface FileTypeMetadataExtractor {
    void extractMetadata(MultipartFile file, MetaData metaData) throws IOException;
}
