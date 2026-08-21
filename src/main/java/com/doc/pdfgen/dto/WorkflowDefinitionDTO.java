package com.doc.pdfgen.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record WorkflowDefinitionDTO(Long id, String name, String description, List<WorkflowStepDTO> steps,
                                    Instant createdAt, Instant updatedAt) {
    public record WorkflowStepDTO(String type, Map<String, Object> config) {}
    public record SaveRequest(String name, String description, List<WorkflowStepDTO> steps) {}
}
