package com.doc.pdfgen.service;

import com.doc.pdfgen.dto.BorderType;
import com.doc.pdfgen.dto.CompressPDFDTO;
import com.doc.pdfgen.dto.DirectoryWorkflowOptions;
import com.doc.pdfgen.dto.ImageToPdfDTO;
import com.doc.pdfgen.dto.RequestTypeDTO;
import com.doc.pdfgen.pdf.service.PDFCompressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DirectoryWorkflowService {
    private final PDFPipeline pdfPipeline;
    private final PDFCompressService compressService;
    private final ObjectMapper objectMapper;

    public DirectoryWorkflowService(PDFPipeline pdfPipeline, PDFCompressService compressService,
                                    ObjectMapper objectMapper) {
        this.pdfPipeline = pdfPipeline;
        this.compressService = compressService;
        this.objectMapper = objectMapper;
    }

    public WorkflowResult run(List<MultipartFile> files, List<String> relativePaths,
                              DirectoryWorkflowOptions options) throws IOException {
        validate(files, relativePaths, options);
        String runId = UUID.randomUUID().toString();
        List<ProcessedFile> outputs = new ArrayList<>();
        List<FileReport> reports = new ArrayList<>();

        for (int index = 0; index < files.size(); index++) {
            MultipartFile file = files.get(index);
            String relativePath = safeRelativePath(relativePaths.get(index));
            try {
                byte[] pdf = convert(file);
                List<String> steps = new ArrayList<>();
                steps.add(isPdf(file) ? "PDF_PASSTHROUGH" : "IMAGE_TO_PDF");
                if (options.isCompress()) {
                    CompressPDFDTO compression = new CompressPDFDTO();
                    compression.setPageSize("A4");
                    compression.setCompressionQuality(options.getCompressionQuality());
                    pdf = compressService.compressBytes(pdf, compression);
                    steps.add("COMPRESS_PDF");
                }
                String outputName = toPdfName(relativePath);
                outputs.add(new ProcessedFile(outputName, pdf));
                reports.add(new FileReport(relativePath, outputName, "COMPLETED", steps, null,
                        file.getSize(), pdf.length));
            } catch (Exception exception) {
                reports.add(new FileReport(relativePath, null, "FAILED", List.of(),
                        safeMessage(exception), file.getSize(), 0));
            }
        }

        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("None of the selected files could be processed");
        }

        int failed = reports.size() - outputs.size();
        if (options.isMerge()) {
            byte[] merged = pdfPipeline.mergePDFs(outputs.stream().map(ProcessedFile::bytes).toList());
            return new WorkflowResult(runId, merged, "workflow-output.pdf", MediaType.APPLICATION_PDF_VALUE,
                    outputs.size(), failed);
        }

        byte[] archive = createArchive(runId, outputs, reports, options);
        return new WorkflowResult(runId, archive, "workflow-output.zip", "application/zip",
                outputs.size(), failed);
    }

    private byte[] convert(MultipartFile file) throws IOException {
        if (isPdf(file)) return file.getBytes();
        String name = lowerName(file);
        if (!(name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"))) {
            throw new IllegalArgumentException("Unsupported file type");
        }
        ImageToPdfDTO imageOptions = new ImageToPdfDTO();
        imageOptions.setPageSize("A4");
        imageOptions.setOrientation("portrait");
        imageOptions.setBorderType(BorderType.INCLUDE_MARGINS);
        RequestTypeDTO request = new RequestTypeDTO();
        request.setImageToPdf(true);
        request.setImageToPdfDTO(imageOptions);
        return pdfPipeline.execute(List.of(file), request);
    }

    private byte[] createArchive(String runId, List<ProcessedFile> outputs, List<FileReport> reports,
                                 DirectoryWorkflowOptions options) throws IOException {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (ProcessedFile output : outputs) {
                zip.putNextEntry(new ZipEntry(output.path()));
                zip.write(output.bytes());
                zip.closeEntry();
            }
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("runId", runId);
            report.put("completedAt", Instant.now().toString());
            report.put("compress", options.isCompress());
            report.put("merge", false);
            report.put("files", reports);
            zip.putNextEntry(new ZipEntry("workflow-report.json"));
            zip.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(report));
            zip.closeEntry();
            zip.finish();
            return bytes.toByteArray();
        }
    }

    private void validate(List<MultipartFile> files, List<String> paths, DirectoryWorkflowOptions options) {
        if (files == null || files.isEmpty()) throw new IllegalArgumentException("Select at least one file");
        if (files.size() > 100) throw new IllegalArgumentException("A workflow can process at most 100 files");
        if (paths == null || paths.size() != files.size()) {
            throw new IllegalArgumentException("Each file must include its relative directory path");
        }
        if (options == null) throw new IllegalArgumentException("Workflow options are required");
        if (options.getCompressionQuality() < 1 || options.getCompressionQuality() > 100) {
            throw new IllegalArgumentException("Compression quality must be between 1 and 100");
        }
        long total = 0;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty() || file.getSize() > 50L * 1024 * 1024) {
                throw new IllegalArgumentException("Files must be non-empty and no larger than 50 MB each");
            }
            total += file.getSize();
        }
        if (total > 200L * 1024 * 1024) throw new IllegalArgumentException("The directory exceeds the 200 MB limit");
    }

    private boolean isPdf(MultipartFile file) {
        return MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(file.getContentType()) || lowerName(file).endsWith(".pdf");
    }

    private String lowerName(MultipartFile file) {
        return file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
    }

    private String safeRelativePath(String path) {
        if (path == null || path.isBlank()) throw new IllegalArgumentException("A relative path is required");
        String normalized = path.replace('\\', '/').replaceAll("^/+", "");
        if (normalized.contains("../") || normalized.equals("..")) {
            throw new IllegalArgumentException("Invalid relative path");
        }
        normalized = normalized.replaceAll("[\r\n\u0000]", "_");
        return normalized.length() > 500 ? normalized.substring(normalized.length() - 500) : normalized;
    }

    private String toPdfName(String path) {
        String result = path.replaceFirst("(?i)\\.[^.]+$", "") + ".pdf";
        return result.replaceAll("[^a-zA-Z0-9._/ -]", "_");
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? "Processing failed" : message.substring(0, Math.min(300, message.length()));
    }

    private record ProcessedFile(String path, byte[] bytes) {}
    public record FileReport(String sourcePath, String outputPath, String status, List<String> steps,
                             String error, long inputBytes, long outputBytes) {}
    public record WorkflowResult(String runId, byte[] bytes, String fileName, String contentType,
                                 int completedFiles, int failedFiles) {}
}
