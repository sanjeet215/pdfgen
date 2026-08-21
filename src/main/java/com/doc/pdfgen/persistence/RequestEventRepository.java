package com.doc.pdfgen.persistence;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Repository
public class RequestEventRepository {
 private final EntityManager em; public RequestEventRepository(EntityManager em){this.em=em;}
 @Transactional public void save(RequestEventEntity event){em.persist(event);}
 public long count(){return em.createQuery("select count(e) from RequestEventEntity e",Long.class).getSingleResult();}
 public List<Object[]> byType(){return em.createQuery("select e.requestType,count(e) from RequestEventEntity e group by e.requestType order by count(e) desc",Object[].class).setMaxResults(30).getResultList();}
 public List<Object[]> byCountry(){return em.createQuery("select e.country,count(e) from RequestEventEntity e group by e.country order by count(e) desc",Object[].class).setMaxResults(30).getResultList();}
 public List<Object[]> recent(){return em.createQuery("select e.occurredAt,e.ipAddress,e.country,e.method,e.path,e.requestType,e.statusCode,e.durationMs,e.tenantId from RequestEventEntity e order by e.occurredAt desc",Object[].class).setMaxResults(200).getResultList();}
}
