package com.doc.pdfgen.pdf.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfUnlockService {

    public UnlockResult unlock(byte[] input, String password) throws IOException {
        try (PDDocument document = PDDocument.load(input, password == null ? "" : password)) {
            if (!document.isEncrypted()) {
                return new UnlockResult(input, false);
            }
            document.setAllSecurityToBeRemoved(true);
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                document.save(output);
                return new UnlockResult(output.toByteArray(), true);
            }
        } catch (InvalidPasswordException exception) {
            throw exception;
        }
    }

    public record UnlockResult(byte[] bytes, boolean wasProtected) {}
}
