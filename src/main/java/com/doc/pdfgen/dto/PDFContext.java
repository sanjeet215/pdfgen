package com.doc.pdfgen.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PDFContext {
    private MultipartFile inputFile;
    private byte[] pdfBytes; // holds intermediate/final PDF in memory
    PDFOperationRequestDTO pdfOperationRequestDTO;

    public PDFContext(MultipartFile inputFile, PDFOperationRequestDTO pdfOperationRequestDTO) {
        this.inputFile = inputFile;
        this.pdfOperationRequestDTO = pdfOperationRequestDTO;
    }
}
