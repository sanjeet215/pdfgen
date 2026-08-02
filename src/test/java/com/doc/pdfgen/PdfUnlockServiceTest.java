package com.doc.pdfgen;

import com.doc.pdfgen.pdf.service.PdfUnlockService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfUnlockServiceTest {
    private final PdfUnlockService service = new PdfUnlockService();

    @Test
    void returnsUnprotectedPdfWithoutRewritingIt() throws Exception {
        byte[] input = createPdf(null);

        PdfUnlockService.UnlockResult result = service.unlock(input, null);

        assertThat(result.wasProtected()).isFalse();
        assertThat(result.bytes()).isEqualTo(input);
    }

    @Test
    void removesPasswordProtectionWhenPasswordIsCorrect() throws Exception {
        byte[] input = createPdf("secret123");

        PdfUnlockService.UnlockResult result = service.unlock(input, "secret123");

        assertThat(result.wasProtected()).isTrue();
        try (PDDocument unlocked = PDDocument.load(result.bytes())) {
            assertThat(unlocked.isEncrypted()).isFalse();
            assertThat(unlocked.getNumberOfPages()).isEqualTo(1);
        }
    }

    @Test
    void rejectsMissingOrIncorrectPassword() throws Exception {
        byte[] input = createPdf("secret123");

        assertThatThrownBy(() -> service.unlock(input, null)).isInstanceOf(InvalidPasswordException.class);
        assertThatThrownBy(() -> service.unlock(input, "wrong")).isInstanceOf(InvalidPasswordException.class);
    }

    private byte[] createPdf(String password) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            if (password != null) {
                StandardProtectionPolicy policy = new StandardProtectionPolicy("owner-secret", password, new AccessPermission());
                policy.setEncryptionKeyLength(128);
                document.protect(policy);
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
