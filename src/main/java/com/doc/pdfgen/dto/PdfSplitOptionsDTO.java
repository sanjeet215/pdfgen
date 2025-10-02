package com.doc.pdfgen.dto;

import lombok.Data;

import java.util.List;

@Data
public class PdfSplitOptionsDTO {

    /**
     * Specific pages or page ranges to extract,
     * Example format: "1-3,5,7-10"
     */
    private String pageRanges;

    /**
     * Split the PDF into chunks of N pages each.
     * If specified, overrides pageRanges.
     */
    private Integer splitEveryNPages;

    /**
     * If true, split PDF based on bookmarks/outlines.
     */
    private boolean splitByBookmark;

    /**
     * If true, extract each page as a separate PDF.
     */
    private boolean extractEveryPage;

    /**
     * Max size in MB for each output PDF.
     * If specified, split to ensure no output exceeds this size.
     */
    private Double maxFileSizeMB;

    /**
     * Output file naming prefix.
     */
    private String outputFilePrefix;

    /**
     * Output file naming suffix.
     */
    private String outputFileSuffix;

    /**
     * Example: numbering style, e.g., "001", "A", etc.
     */
    private String outputFileNumberingStyle;

    /**
     * Optional password to protect resulting split PDFs.
     * If empty or null, no password is set.
     */
    private String password;

    /**
     * List of page numbers or ranges user wants to include
     * This can help UI keep state or transmit detailed selections
     */
    private List<String> pageSelectionList;

    /**
     * Flag indicating if user wants to merge selected splits after splitting
     */
    private boolean mergeAfterSplit;

//    public String getPageRanges() {
//        return pageRanges;
//    }
//
//    public void setPageRanges(String pageRanges) {
//        this.pageRanges = pageRanges;
//    }
//
//    public Integer getSplitEveryNPages() {
//        return splitEveryNPages;
//    }
//
//    public void setSplitEveryNPages(Integer splitEveryNPages) {
//        this.splitEveryNPages = splitEveryNPages;
//    }
//
//    public boolean isSplitByBookmark() {
//        return splitByBookmark;
//    }
//
//    public void setSplitByBookmark(boolean splitByBookmark) {
//        this.splitByBookmark = splitByBookmark;
//    }
//
//    public boolean isExtractEveryPage() {
//        return extractEveryPage;
//    }
//
//    public void setExtractEveryPage(boolean extractEveryPage) {
//        this.extractEveryPage = extractEveryPage;
//    }
//
//    public Double getMaxFileSizeMB() {
//        return maxFileSizeMB;
//    }
//
//    public void setMaxFileSizeMB(Double maxFileSizeMB) {
//        this.maxFileSizeMB = maxFileSizeMB;
//    }
//
//    public String getOutputFilePrefix() {
//        return outputFilePrefix;
//    }
//
//    public void setOutputFilePrefix(String outputFilePrefix) {
//        this.outputFilePrefix = outputFilePrefix;
//    }
//
//    public String getOutputFileSuffix() {
//        return outputFileSuffix;
//    }
//
//    public void setOutputFileSuffix(String outputFileSuffix) {
//        this.outputFileSuffix = outputFileSuffix;
//    }
//
//    public String getOutputFileNumberingStyle() {
//        return outputFileNumberingStyle;
//    }
//
//    public void setOutputFileNumberingStyle(String outputFileNumberingStyle) {
//        this.outputFileNumberingStyle = outputFileNumberingStyle;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public List<String> getPageSelectionList() {
//        return pageSelectionList;
//    }
//
//    public void setPageSelectionList(List<String> pageSelectionList) {
//        this.pageSelectionList = pageSelectionList;
//    }
//
//    public boolean isMergeAfterSplit() {
//        return mergeAfterSplit;
//    }
//
//    public void setMergeAfterSplit(boolean mergeAfterSplit) {
//        this.mergeAfterSplit = mergeAfterSplit;
//    }
}
