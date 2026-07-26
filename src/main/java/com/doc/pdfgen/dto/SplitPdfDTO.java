package com.doc.pdfgen.dto;

import lombok.Data;

@Data
public class SplitPdfDTO {
    private String pages = "";
    private boolean separateFiles = true;
}
