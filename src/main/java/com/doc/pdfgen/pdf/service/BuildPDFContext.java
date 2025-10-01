package com.doc.pdfgen.pdf.service;

import com.doc.pdfgen.dto.PDFContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

@Order(1)
public class BuildPDFContext implements PDFProcessService {

    private static final Logger logger = LoggerFactory.getLogger(BuildPDFContext.class);

    @Override
    public void processPDF(PDFContext pdfContext) {

        try {
            byte[] byteArray = pdfContext.getInputFile().getBytes();
            pdfContext.setPdfBytes(byteArray);
        } catch (Exception exception) {
            // will store the file incase of any exception // after S3 integration
        }
    }
}
