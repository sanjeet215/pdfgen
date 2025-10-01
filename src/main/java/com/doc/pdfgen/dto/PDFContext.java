package com.doc.pdfgen.dto;

import org.springframework.web.multipart.MultipartFile;

public class PDFContext {
    private MultipartFile inputFile;
    private byte[] pdfBytes; // holds intermediate/final PDF in memory
    PDFOperationRequestDTO pdfOperationRequestDTO;

    public PDFContext(MultipartFile inputFile, PDFOperationRequestDTO pdfOperationRequestDTO) {
        this.inputFile = inputFile;
        this.pdfOperationRequestDTO = pdfOperationRequestDTO;
    }

    public MultipartFile getInputFile() {
        return inputFile;
    }

    public byte[] getPdfBytes() {
        return pdfBytes;
    }

    public void setPdfBytes(byte[] pdfBytes) {
        this.pdfBytes = pdfBytes;
    }

    public void setPdfOperationRequestDTO(PDFOperationRequestDTO pdfOperationRequestDTO) {
        this.pdfOperationRequestDTO = pdfOperationRequestDTO;
    }

    public PDFOperationRequestDTO getPdfOperationRequestDTO() {
        return pdfOperationRequestDTO;
    }
}
