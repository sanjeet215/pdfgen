package com.doc.pdfgen.pdf.service;

import com.doc.pdfgen.dto.PDFContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;


/**
 * Compress PDF file and passes the compressed file to next step
 */
@Order(2)
public class PDFCompressService implements PDFProcessService {

    private static final Logger logger = LoggerFactory.getLogger(PDFCompressService.class);

    @Override
    public void processPDF(PDFContext pdfContext) {
        logger.debug(">>compressPdf");

        if (!pdfContext.getPdfOperationRequestDTO().isCompressionRequired()) {
            logger.info("Compression not required, returning original PDF.");
        }
        try {
//            compressPDF(multipartFile, pdfOperationRequestDTO);
        } catch (Exception e) {
            throw new RuntimeException("PDF compression failed", e);
        }
    }

//    private void compressPDF(MultipartFile multipartFile, PDFOperationRequestDTO pdfOperationRequestDTO) throws IOException {
//        float quality = pdfOperationRequestDTO.getCompressionQuality(); // e.g. 0.75f
//        int maxWidth = pdfOperationRequestDTO.getMaxImageWidth();       // e.g. 1600
//        int maxHeight = pdfOperationRequestDTO.getMaxImageHeight();     // e.g. 1600
//
//        try (PDDocument document = PDDocument.load(multipartFile.getInputStream());
//             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//
//            PDPageTree pages = document.getPages();
//            for (PDPage page : pages) {
//                PDResources resources = page.getResources();
//                if (resources == null) continue;
//
//                for (COSName name : resources.getXObjectNames()) {
//                    PDXObject xobject;
//                    try {
//                        xobject = resources.getXObject(name);
//                    } catch (IOException e) {
//                        continue;
//                    }
//                    if (xobject instanceof PDImageXObject) {
//                        PDImageXObject imageObject = (PDImageXObject) xobject;
//                        BufferedImage image = imageObject.getImage();
//                        if (image == null) continue;
//
//                        int origW = image.getWidth();
//                        int origH = image.getHeight();
//                        int targetW = origW;
//                        int targetH = origH;
//
//                        if (maxWidth > 0 && targetW > maxWidth) {
//                            float ratio = (float) maxWidth / targetW;
//                            targetW = maxWidth;
//                            targetH = Math.max(1, Math.round(targetH * ratio));
//                        }
//                        if (maxHeight > 0 && targetH > maxHeight) {
//                            float ratio = (float) maxHeight / targetH;
//                            targetH = maxHeight;
//                            targetW = Math.max(1, Math.round(targetW * ratio));
//                        }
//
//                        BufferedImage processedImage = image;
//                        if (targetW != origW || targetH != origH) {
//                            processedImage = resizeImage(image, targetW, targetH);
//                        }
//
//                        byte[] jpegBytes = bufferedImageToJpegBytes(processedImage, quality);
//
//                        try (ByteArrayInputStream bais = new ByteArrayInputStream(jpegBytes)) {
//                            PDImageXObject newImage = JPEGFactory.createFromStream(document, bais);
//                            resources.put(name, newImage);
//                        }
//                    }
//                }
//            }
//
//            document.save(baos);
//            MultipartFile compressedFile = new MockMultipartFile(multipartFile.getName(), multipartFile.getOriginalFilename(), multipartFile.getContentType(), baos.toByteArray());
//        }
//    }
//
//    private static BufferedImage resizeImage(BufferedImage src, int width, int height) {
//        BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = dest.createGraphics();
//        try {
//            g.setComposite(AlphaComposite.Src);
//            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            g.drawImage(src, 0, 0, width, height, null);
//        } finally {
//            g.dispose();
//        }
//        return dest;
//    }
//
//    private static byte[] bufferedImageToJpegBytes(BufferedImage image, float quality) throws IOException {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
//        if (!writers.hasNext()) throw new IllegalStateException("No JPEG writer found");
//
//        ImageWriter writer = writers.next();
//        ImageWriteParam iwp = writer.getDefaultWriteParam();
//        if (iwp.canWriteCompressed()) {
//            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//            iwp.setCompressionQuality(quality);
//        }
//
//        try (MemoryCacheImageOutputStream ios = new MemoryCacheImageOutputStream(baos)) {
//            writer.setOutput(ios);
//            writer.write(null, new IIOImage(image, null, null), iwp);
//            ios.flush();
//        } finally {
//            writer.dispose();
//        }
//
//        return baos.toByteArray();
//    }
}

