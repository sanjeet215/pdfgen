package com.doc.pdfgen.dto;

public class PDFOperationRequestDTO {
    boolean compressionRequired;
    BorderType borderType;
    String pageSize;


    public boolean isCompressionRequired() {
        return compressionRequired;
    }

    public void setCompressionRequired(boolean compressionRequired) {
        this.compressionRequired = compressionRequired;
    }

    public BorderType getBorderType() {
        return borderType;
    }

    public void setBorderType(BorderType borderType) {
        this.borderType = borderType;
    }

    public String getPageSize() {
        return pageSize;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }
}
