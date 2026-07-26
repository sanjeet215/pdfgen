package com.doc.pdfgen.dto;

import lombok.Data;

@Data
public class ImageToPdfDTO {
    private String fileName;
    private BorderType borderType;
    private String pageSize;
    private String orientation;
    private boolean mergeAll;
}
