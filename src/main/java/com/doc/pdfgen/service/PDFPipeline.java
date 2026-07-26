package com.doc.pdfgen.service;

import com.doc.pdfgen.dto.PDFContext;
import com.doc.pdfgen.dto.RequestTypeDTO;
import com.doc.pdfgen.pdf.service.PDFProcessService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class PDFPipeline {

    private static final Logger logger = LoggerFactory.getLogger(PDFPipeline.class);
    private final List<PDFProcessService> services;

    @Autowired
    public PDFPipeline(List<PDFProcessService> services) {
        this.services = services;
    }

    public byte[] execute(List<MultipartFile> inputFiles, RequestTypeDTO requestTypeDTO) {
        if (inputFiles == null || inputFiles.isEmpty()) {
            throw new IllegalArgumentException("At least one input file is required");
        }
        if (requestTypeDTO == null) {
            throw new IllegalArgumentException("Request options are required");
        }
        List<byte[]> pdfBytesList = new ArrayList<>();
        for (MultipartFile inputFile : inputFiles) {
            pdfBytesList.add(executePipeLine(inputFile, requestTypeDTO));
        }
        if (CollectionUtils.size(inputFiles) > 1) {
            if (requestTypeDTO.getImageToPdfDTO() == null
                    || !requestTypeDTO.getImageToPdfDTO().isMergeAll()) {
                throw new IllegalArgumentException(
                        "Multiple files require the merge-all option for a single PDF response");
            }
            return mergePDFs(pdfBytesList);
        } else {
            return pdfBytesList.get(0);
        }
    }

    public byte[] executePipeLine(MultipartFile inputFile, RequestTypeDTO requestTypeDTO) {
        PDFContext pdfContext = new PDFContext(inputFile, requestTypeDTO);
        services.forEach(service -> service.processPDF(pdfContext));
        return pdfContext.getPdfBytes();
    }

    public byte[] mergePDFs(List<byte[]> pdfBytesList) {
        if (pdfBytesList == null || pdfBytesList.isEmpty()) {
            return new byte[0];
        }
        PDFMergerUtility merger = new PDFMergerUtility();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            for (byte[] pdfBytes : pdfBytesList) {
                if (pdfBytes == null || pdfBytes.length == 0) {
                    throw new IllegalStateException("A pipeline stage produced an empty PDF");
                }
                merger.addSource(new ByteArrayInputStream(pdfBytes));
            }
            merger.setDestinationStream(output);
            merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to merge generated PDFs", exception);
        }
    }
}
