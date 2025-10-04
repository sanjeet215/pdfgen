package com.doc.pdfgen.dto;

import lombok.Data;

@Data
public class CompressPDFDTO {
    String fileName;
    String pageSize;
    Integer maxImageWidth;
    Integer maxImageHeight;
    int compressionQuality;
}
