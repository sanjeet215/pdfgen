package com.doc.pdfgen.service;

import com.doc.pdfgen.dto.PDFContext;
import com.doc.pdfgen.dto.PDFOperationRequestDTO;
import com.doc.pdfgen.pdf.service.PDFProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class PDFPipeline {

    private static final Logger logger = LoggerFactory.getLogger(PDFPipeline.class);
    private final List<PDFProcessService> services;

    @Autowired
    public PDFPipeline(List<PDFProcessService> services) {
        this.services = services;
    }

    public byte[] execute(MultipartFile inputFile, PDFOperationRequestDTO pdfOperationRequestDTO) {
        PDFContext pdfContext = new PDFContext(inputFile, pdfOperationRequestDTO);
        services.forEach(service -> service.processPDF(pdfContext));
        return pdfContext.getPdfBytes();
    }
}
