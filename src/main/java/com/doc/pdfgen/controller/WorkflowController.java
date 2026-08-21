package com.doc.pdfgen.controller;

import com.doc.pdfgen.dto.DirectoryWorkflowOptions;
import com.doc.pdfgen.dto.WorkflowDefinitionDTO;
import com.doc.pdfgen.service.DirectoryWorkflowService;
import com.doc.pdfgen.service.WorkflowDefinitionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    private final DirectoryWorkflowService workflowService;
    private final WorkflowDefinitionService definitionService;

    public WorkflowController(DirectoryWorkflowService workflowService, WorkflowDefinitionService definitionService) {
        this.workflowService = workflowService;
        this.definitionService = definitionService;
    }

    @GetMapping
    public List<WorkflowDefinitionDTO> list() { return definitionService.list(); }

    @GetMapping("/{id}")
    public WorkflowDefinitionDTO get(@PathVariable long id) { return definitionService.get(id); }

    @PostMapping
    public WorkflowDefinitionDTO create(@RequestBody WorkflowDefinitionDTO.SaveRequest request) {
        return definitionService.create(request);
    }

    @PutMapping("/{id}")
    public WorkflowDefinitionDTO update(@PathVariable long id, @RequestBody WorkflowDefinitionDTO.SaveRequest request) {
        return definitionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        definitionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/run", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> run(@RequestPart("files") List<MultipartFile> files,
                                      @RequestParam("relativePaths") List<String> relativePaths,
                                      @RequestParam("workflowId") long workflowId) throws IOException {
        DirectoryWorkflowOptions options = definitionService.executionOptions(workflowId);
        DirectoryWorkflowService.WorkflowResult result = workflowService.run(files, relativePaths, options);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                .header("X-Workflow-Run-Id", result.runId())
                .header("X-Workflow-Completed", Integer.toString(result.completedFiles()))
                .header("X-Workflow-Failed", Integer.toString(result.failedFiles()))
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.bytes());
    }
}
