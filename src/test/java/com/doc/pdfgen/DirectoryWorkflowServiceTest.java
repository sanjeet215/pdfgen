package com.doc.pdfgen;

import com.doc.pdfgen.dto.DirectoryWorkflowOptions;
import com.doc.pdfgen.pdf.service.PDFCompressService;
import com.doc.pdfgen.service.DirectoryWorkflowService;
import com.doc.pdfgen.service.PDFPipeline;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class DirectoryWorkflowServiceTest {
    private final DirectoryWorkflowService service = new DirectoryWorkflowService(
            mock(PDFPipeline.class), mock(PDFCompressService.class), new ObjectMapper());

    @Test
    void createsZipWithRelativeOutputAndAuditReport() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile(
                "files", "invoice.pdf", "application/pdf", "pdf-content".getBytes(StandardCharsets.UTF_8));
        DirectoryWorkflowOptions options = new DirectoryWorkflowOptions();
        options.setCompress(false);
        options.setMerge(false);

        DirectoryWorkflowService.WorkflowResult result = service.run(
                List.of(pdf), List.of("customers/acme/invoice.pdf"), options);

        assertThat(result.contentType()).isEqualTo("application/zip");
        assertThat(result.completedFiles()).isEqualTo(1);
        assertThat(zipEntries(result.bytes())).containsExactly(
                "customers/acme/invoice.pdf", "workflow-report.json");
    }

    @Test
    void rejectsMismatchedRelativePaths() {
        MockMultipartFile pdf = new MockMultipartFile(
                "files", "invoice.pdf", "application/pdf", new byte[]{1});

        assertThatThrownBy(() -> service.run(List.of(pdf), List.of(), new DirectoryWorkflowOptions()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("relative directory path");
    }

    private List<String> zipEntries(byte[] archive) throws Exception {
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) names.add(entry.getName());
        }
        return names;
    }
}
