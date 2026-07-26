package com.doc.pdfgen.controller;

import com.doc.baseservice.Document;
import com.doc.pdfgen.dto.MetaData;
import com.doc.pdfgen.dto.CompressPDFDTO;
import com.doc.pdfgen.dto.RequestTypeDTO;
import com.doc.pdfgen.pdf.service.MetadataService;
import com.doc.pdfgen.service.PDFPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    public PDFController(PDFPipeline pdfPipeline, MetadataService metadataService) {
        this.pdfPipeline = pdfPipeline;
        this.metadataService = metadataService;
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
