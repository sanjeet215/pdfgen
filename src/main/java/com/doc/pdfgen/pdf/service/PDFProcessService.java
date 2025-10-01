package com.doc.pdfgen.pdf.service;

import com.doc.pdfgen.dto.PDFContext;
import com.doc.pdfgen.dto.PDFOperationRequestDTO;
import org.springframework.web.multipart.MultipartFile;

public interface PDFProcessService {
    void processPDF(PDFContext pdfContext);
}
