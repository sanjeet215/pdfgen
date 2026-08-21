package com.doc.pdfgen.service;

import com.doc.pdfgen.dto.DirectoryWorkflowOptions;
import com.doc.pdfgen.dto.WorkflowDefinitionDTO;
import com.doc.pdfgen.persistence.WorkflowDefinitionEntity;
import com.doc.pdfgen.persistence.WorkflowDefinitionRepository;
import com.doc.pdfgen.security.TenantContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class WorkflowDefinitionService {
    private static final Set<String> ALLOWED_STEPS = Set.of("CONVERT_TO_PDF", "COMPRESS_PDF", "MERGE_PDF");
    private final WorkflowDefinitionRepository repository;
    private final ObjectMapper objectMapper;

    public WorkflowDefinitionService(WorkflowDefinitionRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public List<WorkflowDefinitionDTO> list() {
        return repository.findAll(TenantContext.current().tenantId()).stream().map(this::toDto).toList();
    }

    public WorkflowDefinitionDTO get(long id) { return toDto(find(id)); }

    public WorkflowDefinitionDTO create(WorkflowDefinitionDTO.SaveRequest request) {
        validate(request);
        WorkflowDefinitionEntity entity = new WorkflowDefinitionEntity();
        entity.setTenantId(TenantContext.current().tenantId());
        apply(entity, request);
        return toDto(repository.save(entity));
    }

    public WorkflowDefinitionDTO update(long id, WorkflowDefinitionDTO.SaveRequest request) {
        validate(request);
        WorkflowDefinitionEntity entity = find(id);
        apply(entity, request);
        return toDto(repository.save(entity));
    }

    public void delete(long id) { repository.delete(find(id)); }

    public DirectoryWorkflowOptions executionOptions(long id) {
        List<WorkflowDefinitionDTO.WorkflowStepDTO> steps = get(id).steps();
        DirectoryWorkflowOptions options = new DirectoryWorkflowOptions();
        options.setCompress(steps.stream().anyMatch(step -> step.type().equals("COMPRESS_PDF")));
        options.setMerge(steps.stream().anyMatch(step -> step.type().equals("MERGE_PDF")));
        steps.stream().filter(step -> step.type().equals("COMPRESS_PDF")).findFirst().ifPresent(step -> {
            Object quality = step.config() == null ? null : step.config().get("quality");
            if (quality instanceof Number number) options.setCompressionQuality(number.intValue());
        });
        return options;
    }

    private void validate(WorkflowDefinitionDTO.SaveRequest request) {
        if (request == null || request.name() == null || request.name().isBlank() || request.name().length() > 100) {
            throw new IllegalArgumentException("Workflow name must contain 1 to 100 characters");
        }
        if (request.description() != null && request.description().length() > 500) {
            throw new IllegalArgumentException("Workflow description must be 500 characters or fewer");
        }
        if (request.steps() == null || request.steps().isEmpty()) throw new IllegalArgumentException("Add at least one workflow step");
        Set<String> seen = new HashSet<>();
        for (WorkflowDefinitionDTO.WorkflowStepDTO step : request.steps()) {
            if (step == null || !ALLOWED_STEPS.contains(step.type())) throw new IllegalArgumentException("Unsupported workflow step");
            if (!seen.add(step.type())) throw new IllegalArgumentException("A workflow step can only be added once");
        }
        if (!request.steps().get(0).type().equals("CONVERT_TO_PDF")) {
            throw new IllegalArgumentException("Convert to PDF must be the first step");
        }
        int mergeIndex = indexOf(request.steps(), "MERGE_PDF");
        if (mergeIndex >= 0 && mergeIndex != request.steps().size() - 1) {
            throw new IllegalArgumentException("Merge PDF must be the last step");
        }
    }

    private int indexOf(List<WorkflowDefinitionDTO.WorkflowStepDTO> steps, String type) {
        for (int i = 0; i < steps.size(); i++) if (steps.get(i).type().equals(type)) return i;
        return -1;
    }

    private void apply(WorkflowDefinitionEntity entity, WorkflowDefinitionDTO.SaveRequest request) {
        entity.setName(request.name().trim());
        entity.setDescription(request.description() == null ? "" : request.description().trim());
        try { entity.setStepsJson(objectMapper.writeValueAsString(request.steps())); }
        catch (Exception exception) { throw new IllegalStateException("Unable to save workflow steps", exception); }
    }

    private WorkflowDefinitionEntity find(long id) {
        return repository.findById(id, TenantContext.current().tenantId()).orElseThrow(() -> new IllegalArgumentException("Workflow not found"));
    }

    private WorkflowDefinitionDTO toDto(WorkflowDefinitionEntity entity) {
        try {
            List<WorkflowDefinitionDTO.WorkflowStepDTO> steps = objectMapper.readValue(entity.getStepsJson(), new TypeReference<>() {});
            return new WorkflowDefinitionDTO(entity.getId(), entity.getName(), entity.getDescription(), steps,
                    entity.getCreatedAt(), entity.getUpdatedAt());
        } catch (Exception exception) { throw new IllegalStateException("Unable to read workflow steps", exception); }
    }
}
