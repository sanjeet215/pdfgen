package com.doc.pdfgen.dto;

import lombok.Data;

@Data
public class DirectoryWorkflowOptions {
    private boolean compress = true;
    private boolean merge = true;
    private int compressionQuality = 70;
}
