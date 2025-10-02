package com.doc.pdfgen.dto;

import lombok.Data;

@Data
public class PDFOperationRequestDTO {
    boolean compressionRequired;
    int compressionQuality;
    BorderType borderType;
    String pageSize;

    boolean watermarkRequired;
    WaterMarkProp waterMarkProp;

    Integer maxImageWidth;
    Integer maxImageHeight;

//    public boolean isCompressionRequired() {
//        return compressionRequired;
//    }
//
//    public void setCompressionRequired(boolean compressionRequired) {
//        this.compressionRequired = compressionRequired;
//    }
//
//    public int getCompressionQuality() {
//        return compressionQuality;
//    }
//
//    public void setCompressionQuality(int compressionQuality) {
//        this.compressionQuality = compressionQuality;
//    }
//
//    public BorderType getBorderType() {
//        return borderType;
//    }
//
//    public void setBorderType(BorderType borderType) {
//        this.borderType = borderType;
//    }
//
//    public String getPageSize() {
//        return pageSize;
//    }
//
//    public void setPageSize(String pageSize) {
//        this.pageSize = pageSize;
//    }
//
//    public Integer getMaxImageWidth() {
//        return maxImageWidth;
//    }
//
//    public void setMaxImageWidth(Integer maxImageWidth) {
//        this.maxImageWidth = maxImageWidth;
//    }
//
//    public Integer getMaxImageHeight() {
//        return maxImageHeight;
//    }
//
//    public void setMaxImageHeight(Integer maxImageHeight) {
//        this.maxImageHeight = maxImageHeight;
//    }
//
//    public boolean isWatermarkRequired() {
//        return watermarkRequired;
//    }
//
//    public void setWatermarkRequired(boolean watermarkRequired) {
//        this.watermarkRequired = watermarkRequired;
//    }
//
//    public WaterMarkProp getWaterMarkProp() {
//        return waterMarkProp;
//    }
//
//    public void setWaterMarkProp(WaterMarkProp waterMarkProp) {
//        this.waterMarkProp = waterMarkProp;
//    }
}
