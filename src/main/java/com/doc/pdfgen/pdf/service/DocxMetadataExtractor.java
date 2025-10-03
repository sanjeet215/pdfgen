package com.doc.pdfgen.pdf.service;

import com.doc.pdfgen.dto.MetaData;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component("docxExtractor")
public class DocxMetadataExtractor implements FileTypeMetadataExtractor {
    @Override
    public void extractMetadata(MultipartFile file, MetaData metaData) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            POIXMLProperties.CoreProperties props = document.getProperties().getCoreProperties();
            metaData.setTitle(props.getTitle());
            metaData.setCreator(props.getCreator());
            metaData.setSubject(props.getSubject());
            // ... other DOCX-specific metadata
        }
    }
}