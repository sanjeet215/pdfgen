package com.doc.pdfgen.pdf.service;

import com.doc.pdfgen.dto.MetaData;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component("imageExtractor")
public class ImageMetadataExtractor implements FileTypeMetadataExtractor {

    @Override
    public void extractMetadata(MultipartFile file, MetaData metaData) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(file.getBytes())) {
            BufferedImage image = ImageIO.read(bis);
            if (image != null) {
                metaData.setPageSize(image.getWidth() + "x" + image.getHeight() + " pixels");
                metaData.setOrientation(image.getWidth() > image.getHeight() ? "Landscape" : "Portrait");
                // Could add color depth, color model, etc.
            }
        }
    }
}
