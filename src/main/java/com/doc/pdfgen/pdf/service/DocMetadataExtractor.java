package com.doc.pdfgen.pdf.service;

import com.doc.pdfgen.dto.MetaData;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;

@Component("docExtractor")
public class DocMetadataExtractor implements FileTypeMetadataExtractor {
    private static final Logger logger = LoggerFactory.getLogger(DocMetadataExtractor.class);

    @Override
    public void extractMetadata(MultipartFile file, MetaData metaData) throws IOException {
        try (POIFSFileSystem fs = new POIFSFileSystem(file.getInputStream())) {
            DirectoryEntry dir = fs.getRoot();
            if (dir.hasEntry(SummaryInformation.DEFAULT_STREAM_NAME)) {
                try (DocumentInputStream dis = fs.createDocumentInputStream(SummaryInformation.DEFAULT_STREAM_NAME)) {
                    PropertySet ps = new PropertySet(dis);
                    if (ps.isSummaryInformation()) {
                        SummaryInformation si = new SummaryInformation(ps);

                        metaData.setTitle(si.getTitle());
                        metaData.setAuthor(si.getAuthor());
                        metaData.setSubject(si.getSubject());
                        metaData.setKeywords(si.getKeywords());
                        metaData.setCreator(si.getLastAuthor());

                        if (si.getCreateDateTime() != null) {
                            metaData.setCreationDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                    .format(si.getCreateDateTime()));
                        }
                        if (si.getLastSaveDateTime() != null) {
                            metaData.setModificationDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                    .format(si.getLastSaveDateTime()));
                        }
                    }
                }
            }

            // Set file type and size
            metaData.setFileType("application/msword");
            metaData.setFileSize(file.getSize());

        } catch (Exception e) {
            logger.error("Error extracting DOC metadata: {}", e.getMessage());
            throw new IOException("Failed to extract metadata from DOC file", e);
        }
    }
}