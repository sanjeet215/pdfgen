package com.doc.pdfgen.dto;

import lombok.Data;

@Data
public class PageNumberDTO {
    private String position = "bottom-center";
    private int startNumber = 1;
    private int fontSize = 11;
}
