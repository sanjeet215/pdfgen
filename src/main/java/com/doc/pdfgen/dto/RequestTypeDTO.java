package com.doc.pdfgen.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestTypeDTO {
    @JsonProperty("fileName")
    private String fileName;
    
    @JsonProperty("isImageToPdf")
    private boolean imageToPdf;
    
    @JsonProperty("isMergePDF")
    private boolean mergePDF;
    
    @JsonProperty("isSplitPDF")
    private boolean splitPDF;
    
    @JsonProperty("isWatermarkPDF")
    private boolean watermarkPDF;
    
    @JsonProperty("isCompressPDF")
    private boolean compressPDF;
    
    @JsonProperty("isExtractText")
    private boolean extractText;
    
    @JsonProperty("compressionRequired")
    private boolean compressionRequired;
    
    @JsonProperty("compressionQuality")
    private int compressionQuality;
    
    @JsonProperty("watermarkRequired")
    private boolean watermarkRequired;

    @JsonProperty("imageToPdfDTO")
    private ImageToPdfDTO imageToPdfDTO;

    @JsonProperty("compressPDFDTO")
    private CompressPDFDTO compressPDFDTO;

    @JsonProperty("waterMarkProp")
    private WaterMarkProp waterMarkProp;
}
