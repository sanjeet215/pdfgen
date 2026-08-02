package com.doc.pdfgen.pdf.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PdfProtectService {
    private final SecureRandom secureRandom = new SecureRandom();

    public byte[] protect(byte[] input, String password) throws IOException {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("A password is required");
        }
        if (password.length() > 127) {
            throw new IllegalArgumentException("Password must be 127 characters or fewer");
        }
        try (PDDocument document = PDDocument.load(input);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] ownerSecret = new byte[32];
            secureRandom.nextBytes(ownerSecret);
            StandardProtectionPolicy policy = new StandardProtectionPolicy(
                    Base64.getEncoder().encodeToString(ownerSecret), password, new AccessPermission());
            policy.setEncryptionKeyLength(128);
            policy.setPermissions(new AccessPermission());
            document.protect(policy);
            document.save(output);
            return output.toByteArray();
        } catch (InvalidPasswordException exception) {
            throw new IllegalArgumentException("This PDF is already password protected. Unlock it first.", exception);
        }
    }
}
