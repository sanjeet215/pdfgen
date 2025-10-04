package com.doc.pdfgen.dto;

import lombok.Data;

@Data
public class ImageToPdfDTO {
    private String fileName;
    BorderType borderType;
    String pageSize;
}
