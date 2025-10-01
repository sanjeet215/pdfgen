package com.doc.pdfgen.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public final class CommonUtils {

    public static PDRectangle getPDRectangle(String pageSize) {
        PDRectangle page = PDRectangle.A4;
        if(StringUtils.isNotBlank(pageSize)) {
            if(pageSize.equalsIgnoreCase("A4")) {
                page =  new PDRectangle(PDRectangle.A4.getHeight(),PDRectangle.A4.getWidth());
            } else if(pageSize.equalsIgnoreCase("A5")) {
                page =  new PDRectangle(PDRectangle.A5.getHeight(),PDRectangle.A5.getWidth());
            } else if(pageSize.equalsIgnoreCase("LETTER")) {
                page =  new PDRectangle(PDRectangle.LETTER.getHeight(),PDRectangle.LETTER.getWidth());
            } else if (pageSize.equalsIgnoreCase("LEGAL")) {
                page =  new PDRectangle(PDRectangle.LEGAL.getHeight(),PDRectangle.LEGAL.getWidth());
            }
        }
        return page;
    }
}
