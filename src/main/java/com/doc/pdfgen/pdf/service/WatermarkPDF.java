package com.doc.pdfgen.pdf.service;

import com.doc.pdfgen.dto.PDFContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class WatermarkPDF implements PDFProcessService {

    private static final Logger logger = LoggerFactory.getLogger(WatermarkPDF.class);
    @Override
    public void processPDF(PDFContext pdfContext) {
        logger.debug(">>watermarkPdf");
        if(!pdfContext.getPdfOperationRequestDTO().isWatermarkRequired()){
            return;
        }
        logger.debug("<<watermarkPdf");
    }
}


//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfReader;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.layout.Document;
//import com.itextpdf.layout.property.TextAlignment;
//import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
//import com.itextpdf.layout.Canvas;
//import com.itextpdf.kernel.colors.ColorConstants;
//import com.itextpdf.kernel.font.PdfFont;
//import com.itextpdf.kernel.font.PdfFontFactory;
//import com.itextpdf.kernel.geom.PageSize;
//
//public class PdfWatermarkExample {
//
//    public static void addTextWatermark(String src, String dest, String watermarkText) throws Exception {
//        PdfDocument pdfDoc = new PdfDocument(new PdfReader(src), new PdfWriter(dest));
//        Document document = new Document(pdfDoc);
//
//        PdfFont font = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
//
//        int n = pdfDoc.getNumberOfPages();
//        for (int i = 1; i <= n; i++) {
//            PdfCanvas pdfCanvas = new PdfCanvas(pdfDoc.getPage(i));
//            Canvas canvas = new Canvas(pdfCanvas, pdfDoc.getPage(i).getPageSize());
//
//            PageSize pageSize = pdfDoc.getPage(i).getPageSize();
//            float x = (pageSize.getLeft() + pageSize.getRight()) / 2;
//            float y = (pageSize.getTop() + pageSize.getBottom()) / 2;
//
//            canvas.setFontColor(ColorConstants.LIGHT_GRAY);
//            canvas.setFontSize(60);
//            canvas.setFont(font);
//            canvas.showTextAligned(watermarkText, x, y, i, TextAlignment.CENTER, com.itextpdf.layout.property.VerticalAlignment.MIDDLE, (float) Math.toRadians(45));
//            canvas.close();
//        }
//        document.close();
//    }
//
//    public static void main(String[] args) throws Exception {
//        String sourcePdf = "input.pdf";  // path to your PDF file
//        String outputPdf = "watermarked_output.pdf";  // output file
//        String watermark = "CONFIDENTIAL";
//
//        addTextWatermark(sourcePdf, outputPdf, watermark);
//        System.out.println("Watermark added successfully.");
//    }
//}