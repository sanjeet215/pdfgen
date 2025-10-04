package com.doc.pdfgen.controller;

import com.doc.baseservice.Document;
import com.doc.pdfgen.dto.MetaData;
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

    @PostMapping("/imageToPdf")
    public ResponseEntity<byte[]> imageToPdf(@RequestPart("files") List<MultipartFile> multipartFileList, @RequestPart RequestTypeDTO requestTypeDTO) throws IOException {
        logger.debug(">>imageToPdf");
        byte[] pdfBytes =  pdfPipeline.execute(multipartFileList, requestTypeDTO);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"output.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }


//    @PostMapping("/compress")
//    public ResponseEntity<byte[]> compressPdf(@RequestPart("file") MultipartFile multipartFile, @RequestPart RequestTypeDTO requestTypeDTO) throws IOException {
//        logger.debug(">>compressPdf");
//        logger.debug("<<compressPdf");
//        byte[] pdfBytes = PDFPipeline.execute(multipartFile, requestTypeDTO);
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"output.pdf\"")
//                .contentType(MediaType.APPLICATION_PDF)
//                .body(pdfBytes);
//    }
//
//
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

//    @PostMapping("/merge")
//    public ResponseEntity<Document> mergePdf(@RequestParam("files") MultipartFile[] multipartFiles) throws IOException {
//
//    }

    @PostMapping("/test")
    public ResponseEntity<Document> test() throws IOException {
        logger.info(">> test");
        Document document = new Document();
        document.put("data", "test");
        return ResponseEntity.status(HttpStatus.OK).body(document);
    }
}
