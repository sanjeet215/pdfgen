package com.doc.pdfgen;

import com.doc.pdfgen.pdf.service.PdfProtectService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfProtectServiceTest {
    private final PdfProtectService service = new PdfProtectService();

    @Test
    void protectsPdfWithTheChosenPassword() throws Exception {
        byte[] protectedPdf = service.protect(createPdf(), "strong-pass");

        assertThatThrownBy(() -> PDDocument.load(protectedPdf))
                .isInstanceOf(InvalidPasswordException.class);
        try (PDDocument document = PDDocument.load(protectedPdf, "strong-pass")) {
            assertThat(document.isEncrypted()).isTrue();
            assertThat(document.getNumberOfPages()).isEqualTo(1);
        }
    }

    @Test
    void rejectsWrongPasswordAndMissingNewPassword() throws Exception {
        byte[] protectedPdf = service.protect(createPdf(), "strong-pass");

        assertThatThrownBy(() -> PDDocument.load(protectedPdf, "wrong"))
                .isInstanceOf(InvalidPasswordException.class);
        assertThatThrownBy(() -> service.protect(createPdf(), " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private byte[] createPdf() throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(output);
            return output.toByteArray();
        }
    }
}
