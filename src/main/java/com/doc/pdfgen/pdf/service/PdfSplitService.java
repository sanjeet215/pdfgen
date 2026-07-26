package com.doc.pdfgen.pdf.service;

import com.doc.pdfgen.dto.SplitPdfDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PdfSplitService {
    public SplitResult split(byte[] input, SplitPdfDTO options, String baseName) throws IOException {
        try (PDDocument source = PDDocument.load(input)) {
            List<Integer> pages = parsePages(options.getPages(), source.getNumberOfPages());
            if (options.isSeparateFiles()) {
                return new SplitResult(createZip(source, pages, baseName), "application/zip",
                        baseName + "-split.zip");
            }
            return new SplitResult(createPdf(source, pages), "application/pdf",
                    baseName + "-extracted.pdf");
        }
    }

    private List<Integer> parsePages(String expression, int pageCount) {
        if (pageCount < 1) throw new IllegalArgumentException("The PDF has no pages");
        if (expression == null || expression.isBlank()) {
            List<Integer> all = new ArrayList<>();
            for (int index = 0; index < pageCount; index++) all.add(index);
            return all;
        }
        if (expression.length() > 500 || !expression.matches("[\\d,\\-\\s]+")) {
            throw new IllegalArgumentException("Use page numbers and ranges such as 1, 3-5, 8");
        }
        Set<Integer> selected = new LinkedHashSet<>();
        for (String part : expression.split(",")) {
            String value = part.trim();
            if (value.isEmpty()) continue;
            if (value.contains("-")) {
                String[] bounds = value.split("-", -1);
                if (bounds.length != 2) throw new IllegalArgumentException("Invalid page range: " + value);
                int start = parsePage(bounds[0], pageCount);
                int end = parsePage(bounds[1], pageCount);
                if (start > end) throw new IllegalArgumentException("Page range must be ascending: " + value);
                for (int page = start; page <= end; page++) selected.add(page - 1);
            } else {
                selected.add(parsePage(value, pageCount) - 1);
            }
        }
        if (selected.isEmpty()) throw new IllegalArgumentException("Select at least one page");
        return new ArrayList<>(selected);
    }

    private int parsePage(String value, int pageCount) {
        try {
            int page = Integer.parseInt(value.trim());
            if (page < 1 || page > pageCount) {
                throw new IllegalArgumentException("Page " + page + " is outside this " + pageCount + "-page PDF");
            }
            return page;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid page number: " + value);
        }
    }

    private byte[] createPdf(PDDocument source, List<Integer> pages) throws IOException {
        try (PDDocument output = new PDDocument(); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            for (int page : pages) output.importPage(source.getPage(page));
            output.save(bytes);
            return bytes.toByteArray();
        }
    }

    private byte[] createZip(PDDocument source, List<Integer> pages, String baseName) throws IOException {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (int page : pages) {
                zip.putNextEntry(new ZipEntry(baseName + "-page-" + (page + 1) + ".pdf"));
                try (PDDocument output = new PDDocument(); ByteArrayOutputStream pdf = new ByteArrayOutputStream()) {
                    output.importPage(source.getPage(page));
                    output.save(pdf);
                    zip.write(pdf.toByteArray());
                }
                zip.closeEntry();
            }
            zip.finish();
            return bytes.toByteArray();
        }
    }

    public record SplitResult(byte[] bytes, String contentType, String fileName) {}
}
