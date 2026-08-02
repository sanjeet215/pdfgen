package com.doc.pdfgen.controller;

import com.doc.baseservice.Document;
import com.doc.pdfgen.dto.MetaData;
import com.doc.pdfgen.dto.CompressPDFDTO;
import com.doc.pdfgen.dto.PageNumberDTO;
import com.doc.pdfgen.dto.HtmlToPdfDTO;
import com.doc.pdfgen.dto.RequestTypeDTO;
import com.doc.pdfgen.dto.RtfToPdfDTO;
import com.doc.pdfgen.dto.SplitPdfDTO;
import com.doc.pdfgen.dto.WaterMarkProp;
import com.doc.pdfgen.pdf.service.MetadataService;
import com.doc.pdfgen.pdf.service.PDFCompressService;
import com.doc.pdfgen.pdf.service.PdfSplitService;
import com.doc.pdfgen.pdf.service.PdfUnlockService;
import com.doc.pdfgen.pdf.service.PdfProtectService;
import com.doc.pdfgen.service.HtmlRendererClient;
import com.doc.pdfgen.service.PDFPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/pdf")
public class PDFController {
    private static final Logger logger = LoggerFactory.getLogger(PDFController.class);

    private final PDFPipeline pdfPipeline;

    private final MetadataService metadataService;
    private final HtmlRendererClient htmlRendererClient;
    private final PDFCompressService pdfCompressService;
    private final PdfSplitService pdfSplitService;
    private final PdfUnlockService pdfUnlockService;
    private final PdfProtectService pdfProtectService;

    @Autowired
    public PDFController(PDFPipeline pdfPipeline, MetadataService metadataService,
                         HtmlRendererClient htmlRendererClient,
                         PDFCompressService pdfCompressService,
                         PdfSplitService pdfSplitService,
                         PdfUnlockService pdfUnlockService,
                         PdfProtectService pdfProtectService) {
        this.pdfPipeline = pdfPipeline;
        this.metadataService = metadataService;
        this.htmlRendererClient = htmlRendererClient;
        this.pdfCompressService = pdfCompressService;
        this.pdfSplitService = pdfSplitService;
        this.pdfUnlockService = pdfUnlockService;
        this.pdfProtectService = pdfProtectService;
    }

    @PostMapping(value = "/protect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> protectPdf(
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam("password") String password,
            @RequestParam(value = "mergeAll", defaultValue = "false") boolean mergeAll,
            @RequestParam(value = "compress", defaultValue = "true") boolean compress,
            @RequestParam(value = "compressionQuality", defaultValue = "70") int compressionQuality)
            throws IOException {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one PDF file is required");
        }
        if (password == null || password.isBlank() || password.length() > 127) {
            throw new IllegalArgumentException("Enter a password between 1 and 127 characters");
        }
        if (compressionQuality < 1 || compressionQuality > 100) {
            throw new IllegalArgumentException("Compression quality must be between 1 and 100");
        }
        for (MultipartFile file : files) {
            String name = file.getOriginalFilename();
            if (file.isEmpty() || file.getSize() > 50L * 1024 * 1024
                    || name == null || !name.toLowerCase().endsWith(".pdf")) {
                throw new IllegalArgumentException("Only non-empty PDF files up to 50 MB are supported");
            }
        }

        List<byte[]> inputs = new ArrayList<>();
        for (MultipartFile file : files) {
            byte[] bytes = file.getBytes();
            if (compress) {
                CompressPDFDTO options = new CompressPDFDTO();
                options.setFileName(file.getOriginalFilename());
                options.setPageSize("A4");
                options.setCompressionQuality(compressionQuality);
                bytes = pdfCompressService.compressBytes(bytes, options);
            }
            inputs.add(bytes);
        }

        if (mergeAll && inputs.size() > 1) {
            byte[] protectedPdf = pdfProtectService.protect(pdfPipeline.mergePDFs(inputs), password);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"protected-merged.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(protectedPdf);
        }
        if (inputs.size() == 1) {
            String outputName = safePdfName(files.get(0).getOriginalFilename(), "-protected.pdf");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfProtectService.protect(inputs.get(0), password));
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(output)) {
            for (int index = 0; index < inputs.size(); index++) {
                String entryName = safePdfName(files.get(index).getOriginalFilename(), "-protected.pdf");
                zip.putNextEntry(new ZipEntry((index + 1) + "-" + entryName));
                zip.write(pdfProtectService.protect(inputs.get(index), password));
                zip.closeEntry();
            }
            zip.finish();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"protected-pdfs.zip\"")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(output.toByteArray());
        }
    }

    private String safePdfName(String originalName, String suffix) {
        return originalName.replaceFirst("(?i)\\.pdf$", suffix)
                .replaceAll("[\\r\\n\"\\\\/]", "_");
    }

    @PostMapping(value = "/unlock", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> unlockPdf(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "compress", defaultValue = "true") boolean compress,
            @RequestParam(value = "compressionQuality", defaultValue = "70") int compressionQuality)
            throws IOException {
        if (file == null || file.isEmpty() || file.getSize() > 50L * 1024 * 1024) {
            throw new IllegalArgumentException("A non-empty PDF smaller than 50 MB is required");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }
        if (compressionQuality < 1 || compressionQuality > 100) {
            throw new IllegalArgumentException("Compression quality must be between 1 and 100");
        }

        PdfUnlockService.UnlockResult result;
        try {
            result = pdfUnlockService.unlock(file.getBytes(), password);
        } catch (InvalidPasswordException exception) {
            return ResponseEntity.unprocessableEntity()
                    .header("X-Password-Required", "true")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("This PDF is password protected. Enter the correct password.".getBytes());
        }

        byte[] output = result.bytes();
        if (compress) {
            CompressPDFDTO options = new CompressPDFDTO();
            options.setFileName(originalName);
            options.setPageSize("A4");
            options.setCompressionQuality(compressionQuality);
            output = pdfCompressService.compressBytes(output, options);
        }

        String outputName = originalName.replaceFirst("(?i)\\.pdf$", "-unlocked.pdf")
                .replaceAll("[\\r\\n\"\\\\/]", "_");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputName + "\"")
                .header("X-Was-Protected", Boolean.toString(result.wasProtected()))
                .contentType(MediaType.APPLICATION_PDF)
                .body(output);
    }

    @PostMapping(value = "/merge", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> mergePdf(
            @RequestPart("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one PDF file is required");
        }
        for (MultipartFile file : files) {
            if (file.isEmpty()
                    || (!MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(file.getContentType())
                    && (file.getOriginalFilename() == null
                    || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")))) {
                throw new IllegalArgumentException("Only non-empty PDF files are supported");
            }
        }

        RequestTypeDTO request = new RequestTypeDTO();
        request.setMergePDF(true);
        byte[] pdfBytes = pdfPipeline.execute(files, request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"merged.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping(value = "/compress", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> compressPdf(
            @RequestPart("files") List<MultipartFile> files,
            @RequestPart("compressPDFDTO") CompressPDFDTO options) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one PDF file is required");
        }
        if (options == null || options.getCompressionQuality() < 1
                || options.getCompressionQuality() > 100) {
            throw new IllegalArgumentException("Compression quality must be between 1 and 100");
        }
        for (MultipartFile file : files) {
            if (file.isEmpty()
                    || (!MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(file.getContentType())
                    && (file.getOriginalFilename() == null
                    || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")))) {
                throw new IllegalArgumentException("Only non-empty PDF files are supported");
            }
        }

        RequestTypeDTO request = new RequestTypeDTO();
        request.setCompressPDF(true);
        request.setCompressionRequired(true);
        request.setCompressPDFDTO(options);

        List<byte[]> compressedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            compressedFiles.add(pdfPipeline.execute(List.of(file), request));
        }

        if (options.isMergeAll() && compressedFiles.size() > 1) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"compressed-merged.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfPipeline.mergePDFs(compressedFiles));
        }
        if (compressedFiles.size() == 1) {
            String originalName = files.get(0).getOriginalFilename();
            String outputName = originalName == null ? "compressed.pdf"
                    : originalName.replaceFirst("(?i)\\.pdf$", "") + "-compressed.pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(compressedFiles.get(0));
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(output)) {
            for (int index = 0; index < compressedFiles.size(); index++) {
                String originalName = files.get(index).getOriginalFilename();
                String entryName = originalName == null ? "compressed-" + (index + 1) + ".pdf"
                        : (index + 1) + "-" + originalName.replaceFirst("(?i)\\.pdf$", "") + "-compressed.pdf";
                zip.putNextEntry(new ZipEntry(entryName));
                zip.write(compressedFiles.get(index));
                zip.closeEntry();
            }
            zip.finish();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"compressed-pdfs.zip\"")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(output.toByteArray());
        }
    }

    @PostMapping(value = "/page-numbers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> addPageNumbers(
            @RequestPart("file") MultipartFile file,
            @RequestPart("pageNumberDTO") PageNumberDTO options) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("A PDF file is required");
        }
        if ((!MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(file.getContentType())
                && (file.getOriginalFilename() == null
                || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")))) {
            throw new IllegalArgumentException("Only a non-empty PDF file is supported");
        }
        if (options == null || options.getStartNumber() < 0
                || options.getFontSize() < 8 || options.getFontSize() > 24) {
            throw new IllegalArgumentException("Invalid page number settings");
        }

        RequestTypeDTO request = new RequestTypeDTO();
        request.setPageNumberPDF(true);
        request.setPageNumberDTO(options);
        byte[] output = pdfPipeline.execute(List.of(file), request);
        String originalName = file.getOriginalFilename();
        String outputName = originalName == null ? "numbered.pdf"
                : originalName.replaceFirst("(?i)\\.pdf$", "") + "-numbered.pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(output);
    }

    @PostMapping(value = "/html-to-pdf", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> htmlToPdf(@RequestBody HtmlToPdfDTO options) {
        if (options == null || options.getHtml() == null || options.getHtml().isBlank()) {
            throw new IllegalArgumentException("HTML content is required");
        }
        if (options.getHtml().length() > 1_500_000) {
            throw new IllegalArgumentException("HTML content is too large");
        }
        if (!List.of("A4", "Letter", "Legal").contains(options.getPageSize())
                || !List.of("portrait", "landscape").contains(options.getOrientation())
                || options.getMarginMm() < 0 || options.getMarginMm() > 50) {
            throw new IllegalArgumentException("Invalid PDF layout settings");
        }

        byte[] output = htmlRendererClient.render(options);
        if (options.isCompress()) {
            CompressPDFDTO compression = new CompressPDFDTO();
            compression.setFileName(options.getFileName());
            compression.setPageSize(options.getPageSize());
            compression.setCompressionQuality(
                    Math.min(100, Math.max(1, options.getCompressionQuality())));
            output = pdfCompressService.compressBytes(output, compression);
        }

        String outputName = options.getFileName() == null || options.getFileName().isBlank()
                ? "converted.pdf"
                : options.getFileName().replaceFirst("(?i)\\.pdf$", "") + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(output);
    }

    @PostMapping(value = "/rtf-to-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> rtfToPdf(
            @RequestPart("file") MultipartFile file,
            @RequestPart("options") RtfToPdfDTO options) throws IOException {
        if (file == null || file.isEmpty() || file.getSize() > 1_000_000) {
            throw new IllegalArgumentException("A non-empty RTF file smaller than 1 MB is required");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".rtf")) {
            throw new IllegalArgumentException("Only RTF files are supported");
        }
        if (options == null
                || !List.of("A4", "Letter", "Legal").contains(options.getPageSize())
                || !List.of("portrait", "landscape").contains(options.getOrientation())
                || options.getMarginMm() < 0 || options.getMarginMm() > 50) {
            throw new IllegalArgumentException("Invalid PDF layout settings");
        }

        byte[] output = htmlRendererClient.renderRtf(file.getBytes(), options);
        if (options.isCompress()) {
            CompressPDFDTO compression = new CompressPDFDTO();
            compression.setFileName(options.getFileName());
            compression.setPageSize(options.getPageSize());
            compression.setCompressionQuality(
                    Math.min(100, Math.max(1, options.getCompressionQuality())));
            output = pdfCompressService.compressBytes(output, compression);
        }

        String outputName = options.getFileName() == null || options.getFileName().isBlank()
                ? originalName.replaceFirst("(?i)\\.rtf$", "") + ".pdf"
                : options.getFileName().replaceFirst("(?i)\\.pdf$", "") + ".pdf";
        outputName = outputName.replaceAll("[\\r\\n\"\\\\/]", "_");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(output);
    }

    @PostMapping(value = "/split", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> splitPdf(
            @RequestPart("file") MultipartFile file,
            @RequestPart("options") SplitPdfDTO options) throws IOException {
        if (file == null || file.isEmpty() || file.getSize() > 50L * 1024 * 1024) {
            throw new IllegalArgumentException("A non-empty PDF smaller than 50 MB is required");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }
        String baseName = originalName.replaceFirst("(?i)\\.pdf$", "")
                .replaceAll("[\\r\\n\"\\\\/]", "_");
        PdfSplitService.SplitResult result = pdfSplitService.split(file.getBytes(), options, baseName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.fileName() + "\"")
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.bytes());
    }

    @PostMapping(value = "/watermark", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> watermarkPdf(
            @RequestPart("file") MultipartFile file,
            @RequestPart("options") WaterMarkProp options) {
        if (file == null || file.isEmpty() || file.getSize() > 50L * 1024 * 1024) {
            throw new IllegalArgumentException("A non-empty PDF smaller than 50 MB is required");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }
        if (options == null || options.getWatermarkText() == null
                || options.getWatermarkText().isBlank()
                || options.getWatermarkText().length() > 100
                || options.getWatermarkText().chars().anyMatch(character -> character > 255)
                || options.getWatermarkFontSize() == null
                || options.getWatermarkFontSize() < 12 || options.getWatermarkFontSize() > 120
                || options.getWatermarkAngle() == null
                || options.getWatermarkAngle() < -90 || options.getWatermarkAngle() > 90) {
            throw new IllegalArgumentException("Invalid watermark settings");
        }
        RequestTypeDTO request = new RequestTypeDTO();
        request.setWatermarkPDF(true);
        request.setWatermarkRequired(true);
        request.setWaterMarkProp(options);
        byte[] output = pdfPipeline.execute(List.of(file), request);
        String outputName = originalName.replaceFirst("(?i)\\.pdf$", "") + "-watermarked.pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(output);
    }

    @PostMapping(value = "/imageToPdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> imageToPdf(
            @RequestPart("files") List<MultipartFile> multipartFileList,
            @RequestPart(value = "requestTypeDTO", required = true) RequestTypeDTO requestTypeDTO) throws IOException {
        if (requestTypeDTO == null) {
            throw new IllegalArgumentException("Request type DTO cannot be null");
        }
        logger.debug(">>imageToPdf");
        logger.info("--imageToPdf(): requestTypeDTO: {}",requestTypeDTO);
        byte[] pdfBytes =  pdfPipeline.execute(multipartFileList, requestTypeDTO);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"output.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }


//    @PostMapping("/split")
//    public ResponseEntity<Document> splitPdf(@RequestParam("file") MultipartFile multipartFile,
//                                             @RequestParam(value = "ranges", required = false) List<String> ranges) throws IOException {
//
//        logger.debug(">> SplitPdf fileName: {},ranges: {}",multipartFile.getOriginalFilename(),ranges);
//        //pdfSplitterService.splitPdfByRange(multipartFile, AppConstants.SPLITTED_OUTPUT_DIR,ranges);
//        Document document = new Document();
//        document.put("data", "test");
//        return ResponseEntity.status(HttpStatus.OK).body(document);
//    }

    @PostMapping("/extract")
    public ResponseEntity<MetaData> extractMetadata(@RequestParam("file") MultipartFile file) {
        try {
            MetaData metadata = metadataService.extractFileMetadata(file);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/test")
    public ResponseEntity<Document> test() throws IOException {
        logger.info(">> test");
        Document document = new Document();
        document.put("data", "test");
        return ResponseEntity.status(HttpStatus.OK).body(document);
    }
}
