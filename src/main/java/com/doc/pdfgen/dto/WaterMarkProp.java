package com.doc.pdfgen.dto;

import lombok.Data;

@Data
public class WaterMarkProp {
    String watermarkText;
    String watermarkFont;
    Integer watermarkFontSize;
    String watermarkFontColor;
    String watermarkBackgroundColor;
    String watermarkPosition;
    String watermarkAlignment;
    Integer watermarkAngle;
    Integer watermarkPadding;
    String watermarkRotation;
    String watermarkOpacity;
    String watermarkColor;
    String watermarkBorderColor;
    String watermarkBorderWidth;
}
