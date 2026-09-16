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
 public List<Object[]> byMode(){return em.createQuery("select e.processingMode,count(e) from RequestEventEntity e where e.processingMode is not null group by e.processingMode order by count(e) desc",Object[].class).getResultList();}
 public long visitorCount(){return em.createQuery("select count(distinct e.visitorId) from RequestEventEntity e where e.visitorId is not null",Long.class).getSingleResult();}
 public List<String> returningVisitorIds(){return em.createQuery("select e.visitorId from RequestEventEntity e where e.visitorId is not null and e.sessionId is not null group by e.visitorId having count(distinct e.sessionId)>1",String.class).getResultList();}
 public long sessionCount(){return em.createQuery("select count(distinct e.sessionId) from RequestEventEntity e where e.sessionId is not null",Long.class).getSingleResult();}
 public long toolUseCount(){return em.createQuery("select count(e) from RequestEventEntity e where e.toolUsed=true and e.requestType in ('TOOL_USED','PDF_TOOL','WORKFLOW_RUN')",Long.class).getSingleResult();}
 public long downloadCount(){return em.createQuery("select count(e) from RequestEventEntity e where e.requestType='DOWNLOAD'",Long.class).getSingleResult();}
 public long usefulVisitCount(){return em.createQuery("select count(distinct e.sessionId) from RequestEventEntity e where e.sessionId is not null and e.requestType='DOWNLOAD' and e.sessionId in (select f.sessionId from RequestEventEntity f where f.requestType='FILE_SELECTED')",Long.class).getSingleResult();}
 public List<Object[]> recent(){return em.createQuery("select e.id,e.occurredAt,e.ipAddress,e.country,e.path,e.requestType,e.toolName,e.processingMode,e.toolUsed,e.timeOnPageMs,e.nextAction,e.visitorId,e.sessionId,e.screenWidth,e.screenHeight,e.referer,e.userAgent,e.statusCode,e.durationMs,e.tenantId,e.userId from RequestEventEntity e order by e.occurredAt desc,e.id desc",Object[].class).setMaxResults(200).getResultList();}
}
