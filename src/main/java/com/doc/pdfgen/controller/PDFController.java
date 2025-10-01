package com.doc.pdfgen.controller;

import com.doc.baseservice.Document;
import com.doc.pdfgen.dto.PDFOperationRequestDTO;
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
public class PDFController {
    private static final Logger logger = LoggerFactory.getLogger(PDFController.class);

    private final PDFPipeline PDFPipeline;

    @Autowired
    public PDFController(PDFPipeline PDFPipeline) {
        this.PDFPipeline = PDFPipeline;
    }

    @PostMapping("/compress")
    public ResponseEntity<byte[]> compressPdf(@RequestPart("file") MultipartFile multipartFile, @RequestPart PDFOperationRequestDTO pdfOperationRequestDTO) throws IOException {
        logger.debug(">>compressPdf");
        logger.debug("<<compressPdf");
        byte[] pdfBytes = PDFPipeline.execute(multipartFile,pdfOperationRequestDTO);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"output.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }


    @PostMapping("/split")
    public ResponseEntity<Document> splitPdf(@RequestParam("file") MultipartFile multipartFile,
                                             @RequestParam(value = "ranges", required = false) List<String> ranges) throws IOException {

        logger.debug(">> SplitPdf fileName: {},ranges: {}",multipartFile.getOriginalFilename(),ranges);
        //pdfSplitterService.splitPdfByRange(multipartFile, AppConstants.SPLITTED_OUTPUT_DIR,ranges);
        Document document = new Document();
        document.put("data", "test");
        return ResponseEntity.status(HttpStatus.OK).body(document);
    }

//    @PostMapping("/merge")
//    public ResponseEntity<Document> mergePdf(@RequestParam("files") MultipartFile[] multipartFiles) throws IOException {}

    @PostMapping("/test")
    public ResponseEntity<Document> test() throws IOException {
        logger.info(">> test");
        Document document = new Document();
        document.put("data", "test");
        return ResponseEntity.status(HttpStatus.OK).body(document);
    }
}
