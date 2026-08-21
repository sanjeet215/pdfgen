package com.doc.pdfgen.persistence;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class WorkflowDefinitionRepository {
    private final EntityManager entityManager;

    public WorkflowDefinitionRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<WorkflowDefinitionEntity> findAll(long tenantId) {
        return entityManager.createQuery(
                "select workflow from WorkflowDefinitionEntity workflow where workflow.tenantId=:tenantId order by workflow.updatedAt desc",
                WorkflowDefinitionEntity.class).setParameter("tenantId", tenantId).getResultList();
    }

    public Optional<WorkflowDefinitionEntity> findById(long id, long tenantId) {
        return entityManager.createQuery("select workflow from WorkflowDefinitionEntity workflow where workflow.id=:id and workflow.tenantId=:tenantId",WorkflowDefinitionEntity.class)
                .setParameter("id",id).setParameter("tenantId",tenantId).getResultStream().findFirst();
    }

    @Transactional
    public WorkflowDefinitionEntity save(WorkflowDefinitionEntity entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
            return entity;
        }
        return entityManager.merge(entity);
    }

    @Transactional
    public void delete(WorkflowDefinitionEntity entity) {
        WorkflowDefinitionEntity attached = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(attached);
    }
}
