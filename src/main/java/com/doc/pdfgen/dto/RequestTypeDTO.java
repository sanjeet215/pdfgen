package com.doc.pdfgen.dto;

import lombok.Data;

@Data
public class RequestTypeDTO {
    String fileName;
    boolean isImageToPdf;
    boolean isMergePDF;
    boolean isSplitPDF;
    boolean isWatermarkPDF;
    boolean isCompressPDF;
    boolean isExtractText;
    boolean compressionRequired;
    int compressionQuality;
    boolean watermarkRequired;

    ImageToPdfDTO imageToPdfDTO;

    CompressPDFDTO compressPDFDTO;

    WaterMarkProp waterMarkProp;
}
