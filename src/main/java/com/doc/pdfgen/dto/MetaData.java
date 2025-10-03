package com.doc.pdfgen.dto;

import lombok.Data;

@Data
public class MetaData {
    private String title;
    private String author;
    private String subject;
    private String keywords;
    private String creator; // Software used to create the file
    private String producer; // Producer of the file
    private String creationDate; // Date the file was created
    private String modificationDate; // Date the file was last modified
    private int pageCount; // Total number of pages
    private String pageSize; // Dimensions of the page (e.g., A4, Letter)
    private String orientation; // Page orientation (e.g., Portrait, Landscape)
    private boolean isEncrypted; // Whether the file is encrypted
    private boolean isPasswordProtected; // Whether the file is password-protected
    private String password; // Password for the file (if applicable)
    private String fileType; // Type of the file (e.g., PDF, DOCX)
    private long fileSize; // Size of the file in bytes
    private String language; // Language of the document content
    private String fontDetails; // Details about fonts used in the document
}
