package com.doc.pdfgen.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PDFContext {
    private MultipartFile inputFile;
    private byte[] pdfBytes; // holds intermediate/final PDF in memory

    RequestTypeDTO requestTypeDTO;

    public PDFContext(MultipartFile inputFile, RequestTypeDTO requestTypeDTO) {
        this.inputFile = inputFile;
        this.requestTypeDTO = requestTypeDTO;
    }
}
