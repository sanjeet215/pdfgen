package com.doc.pdfgen.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public final class CommonUtils {

    public static PDRectangle getPDRectangle(String pageSize) {
        return getPDRectangle(pageSize, "portrait");
    }

    public static PDRectangle getPDRectangle(String pageSize, String orientation) {
        PDRectangle page = PDRectangle.A4;
        if(StringUtils.isNotBlank(pageSize)) {
            if(pageSize.equalsIgnoreCase("A4")) {
                page = PDRectangle.A4;
            } else if(pageSize.equalsIgnoreCase("A5")) {
                page = PDRectangle.A5;
            } else if(pageSize.equalsIgnoreCase("LETTER")) {
                page = PDRectangle.LETTER;
            } else if (pageSize.equalsIgnoreCase("LEGAL")) {
                page = PDRectangle.LEGAL;
            }
        }
        if ("landscape".equalsIgnoreCase(orientation)) {
            return new PDRectangle(page.getHeight(), page.getWidth());
        }
        return page;
    }
}
