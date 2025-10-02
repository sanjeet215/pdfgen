package com.doc.pdfgen.pdf.service;

import com.doc.pdfgen.common.CommonUtils;
import com.doc.pdfgen.dto.PDFContext;
import com.doc.pdfgen.dto.PDFOperationRequestDTO;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Compress PDF file by compressing embedded images.
 * Works with image-only PDFs, text-only PDFs, and PDFs with mixed content.
 * Text content is preserved as-is while images are compressed.
 */
@Component
@Order(2)
public class PDFCompressService implements PDFProcessService {

    private static final Logger logger = LoggerFactory.getLogger(PDFCompressService.class);

    @Override
    public void processPDF(PDFContext pdfContext) {
        logger.debug(">>compressPdf");

        if (!pdfContext.getPdfOperationRequestDTO().isCompressionRequired()) {
            logger.info("Compression not required, skipping compression step.");
            return;
        }

        try {
            compressPDF(pdfContext);
        } catch (Exception e) {
            logger.error("PDF compression failed", e);
            throw new RuntimeException("PDF compression failed", e);
        }
    }

    private void compressPDF(PDFContext pdfContext) throws IOException {
        PDFOperationRequestDTO requestDTO = pdfContext.getPdfOperationRequestDTO();
        byte[] pdfBytes = pdfContext.getPdfBytes();

        float quality = (float) requestDTO.getCompressionQuality() / 100;

        // Determine max dimensions: use provided values or calculate from page size
        int maxWidth;
        int maxHeight;

        if (requestDTO.getMaxImageWidth() != null && requestDTO.getMaxImageHeight() != null) {
            maxWidth = requestDTO.getMaxImageWidth();
            maxHeight = requestDTO.getMaxImageHeight();
            logger.debug("Using provided max dimensions: {}x{}", maxWidth, maxHeight);
        } else {
            // Calculate from page size
            PDRectangle pageRect = CommonUtils.getPDRectangle(requestDTO.getPageSize());
            maxWidth = (int) pageRect.getWidth();
            maxHeight = (int) pageRect.getHeight();
            logger.debug("Calculated max dimensions from page size '{}': {}x{}",
                    requestDTO.getPageSize(), maxWidth, maxHeight);
        }

        // Tracking variables
        int totalImagesFound = 0;
        int imagesCompressed = 0;
        long originalSize = pdfBytes.length;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes);
             PDDocument document = PDDocument.load(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPageTree pages = document.getPages();
            int pageCount = document.getNumberOfPages();
            logger.info("Processing PDF with {} page(s)", pageCount);

            for (PDPage page : pages) {
                PDResources resources = page.getResources();
                if (resources == null) {
                    continue;
                }

                for (COSName name : resources.getXObjectNames()) {
                    PDXObject xobject;
                    try {
                        xobject = resources.getXObject(name);
                    } catch (IOException e) {
                        logger.warn("Failed to get XObject '{}': {}", name.getName(), e.getMessage());
                        continue;
                    }

                    if (xobject instanceof PDImageXObject imageObject) {
                        totalImagesFound++;

                        BufferedImage image = imageObject.getImage();
                        if (image == null) {
                            logger.warn("Image '{}' could not be read, skipping", name.getName());
                            continue;
                        }

                        int originalWidth = image.getWidth();
                        int originalHeight = image.getHeight();
                        int targetWidth = originalWidth;
                        int targetHeight = originalHeight;

                        // Calculate target dimensions if image exceeds max dimensions
                        if (maxWidth > 0 && targetWidth > maxWidth) {
                            float ratio = (float) maxWidth / targetWidth;
                            targetWidth = maxWidth;
                            targetHeight = Math.max(1, Math.round(targetHeight * ratio));
                        }
                        if (maxHeight > 0 && targetHeight > maxHeight) {
                            float ratio = (float) maxHeight / targetHeight;
                            targetHeight = maxHeight;
                            targetWidth = Math.max(1, Math.round(targetWidth * ratio));
                        }

                        // Resize image if dimensions changed
                        BufferedImage processedImage = image;
                        boolean wasResized = false;
                        if (targetWidth != originalWidth || targetHeight != originalHeight) {
                            processedImage = resizeImage(image, targetWidth, targetHeight);
                            wasResized = true;
                            logger.debug("Image '{}' resized from {}x{} to {}x{}",
                                    name.getName(), originalWidth, originalHeight, targetWidth, targetHeight);
                        }

                        // Convert to JPEG with quality compression
                        byte[] jpegBytes = bufferedImageToJpegBytes(processedImage, quality);

                        // Replace image in PDF
                        try (ByteArrayInputStream imageBais = new ByteArrayInputStream(jpegBytes)) {
                            PDImageXObject newImage = JPEGFactory.createFromStream(document, imageBais);
                            resources.put(name, newImage);
                            imagesCompressed++;

                            if (wasResized) {
                                logger.debug("Image '{}' compressed and resized successfully", name.getName());
                            } else {
                                logger.debug("Image '{}' compressed (quality: {}%) without resizing",
                                        name.getName(), (int) (quality * 100));
                            }
                        }
                    }
                }
            }

            // Save compressed PDF to byte array (even if no images were found, to preserve text content)
            document.save(baos);
            byte[] compressedPdfBytes = baos.toByteArray();
            long compressedSize = compressedPdfBytes.length;

            // Set the result back to context
            pdfContext.setPdfBytes(compressedPdfBytes);

            // Log summary
            logCompressionSummary(totalImagesFound, imagesCompressed, originalSize, compressedSize, pageCount);
        }
    }

    /**
     * Logs a detailed summary of the compression operation
     */
    private void logCompressionSummary(int totalImagesFound, int imagesCompressed,
                                       long originalSize, long compressedSize, int pageCount) {
        if (totalImagesFound == 0) {
            logger.info("PDF compression completed: No images found in {} page(s). " +
                            "Text-only PDF preserved as-is. Size: {} bytes",
                    pageCount, compressedSize);
        } else if (imagesCompressed == 0) {
            logger.warn("PDF compression completed: Found {} image(s) but none could be compressed. " +
                            "Original size: {} bytes, Final size: {} bytes",
                    totalImagesFound, originalSize, compressedSize);
        } else {
            long sizeSaved = originalSize - compressedSize;
            String compressionRatioFormatted = String.format("%.2f", (sizeSaved * 100.0) / originalSize);
            logger.info("PDF compression completed successfully: " +
                            "Compressed {}/{} image(s) across {} page(s). " +
                            "Original size: {} bytes, Compressed size: {} bytes, " +
                            "Saved: {} bytes ({}% reduction)",
                    imagesCompressed, totalImagesFound, pageCount,
                    originalSize, compressedSize, sizeSaved, compressionRatioFormatted);
        }
    }

    /**
     * Resizes a BufferedImage to specified dimensions using high-quality interpolation
     */
    private BufferedImage resizeImage(BufferedImage src, int width, int height) {
        BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dest.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(src, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return dest;
    }

    /**
     * Converts a BufferedImage to JPEG byte array with specified quality
     */
    private byte[] bufferedImageToJpegBytes(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPEG writer found");
        }

        ImageWriter writer = writers.next();
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        if (iwp.canWriteCompressed()) {
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(quality);
        }

        try (MemoryCacheImageOutputStream ios = new MemoryCacheImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), iwp);
            ios.flush();
        } finally {
            writer.dispose();
        }

        return baos.toByteArray();
    }
}