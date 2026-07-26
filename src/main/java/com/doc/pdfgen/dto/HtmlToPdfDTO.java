package com.doc.pdfgen.dto;

import lombok.Data;

@Data
public class HtmlToPdfDTO {
    private String html;
    private String fileName = "converted.pdf";
    private String pageSize = "A4";
    private String orientation = "portrait";
    private int marginMm = 12;
    private boolean compress = true;
    private int compressionQuality = 70;
}
