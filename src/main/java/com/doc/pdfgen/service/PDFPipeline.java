package com.doc.pdfgen.service;

import com.doc.pdfgen.dto.PDFContext;
import com.doc.pdfgen.dto.RequestTypeDTO;
import com.doc.pdfgen.pdf.service.PDFProcessService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Component
public class PDFPipeline {

    private static final Logger logger = LoggerFactory.getLogger(PDFPipeline.class);
    private final List<PDFProcessService> services;

    @Autowired
    public PDFPipeline(List<PDFProcessService> services) {
        this.services = services;
    }

    public byte[] execute(List<MultipartFile> inputFiles, RequestTypeDTO requestTypeDTO) {
        List<byte[]> pdfBytesList = new ArrayList<>();
        for (MultipartFile inputFile : inputFiles) {
            pdfBytesList.add(executePipeLine(inputFile, requestTypeDTO));
        }
        if (CollectionUtils.size(inputFiles) > 1) {
            return zipPDFs(pdfBytesList);
        } else {
            return pdfBytesList.get(0);
        }
    }

    public byte[] executePipeLine(MultipartFile inputFile, RequestTypeDTO requestTypeDTO) {
        PDFContext pdfContext = new PDFContext(inputFile, requestTypeDTO);
        services.forEach(service -> service.processPDF(pdfContext));
        return pdfContext.getPdfBytes();
    }

    //Zipping the PDFs in the list
    public byte[] zipPDFs(List<byte[]> pdfBytesList) {
        return pdfBytesList.stream().reduce(new byte[0], (a, b) -> {
            byte[] result = new byte[a.length + b.length];
            System.arraycopy(a, 0, result, 0, a.length);
            System.arraycopy(b, 0, result, a.length, b.length);
            return result;
        });
    }
}
