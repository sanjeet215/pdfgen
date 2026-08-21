package com.doc.pdfgen.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="request_events", indexes={@Index(name="idx_request_event_time",columnList="occurredAt"),@Index(name="idx_request_event_tenant",columnList="tenantId")})
public class RequestEventEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long tenantId; private Long userId; @Column(nullable=false) private Instant occurredAt;
 @Column(nullable=false,length=10) private String method; @Column(nullable=false,length=300) private String path;
 @Column(nullable=false,length=80) private String requestType; @Column(nullable=false) private int statusCode;
 @Column(nullable=false) private long durationMs; @Column(length=64) private String ipAddress; @Column(length=2) private String country;
 @Column(length=300) private String userAgent;
 public void setTenantId(Long v){tenantId=v;} public void setUserId(Long v){userId=v;} public void setOccurredAt(Instant v){occurredAt=v;}
 public void setMethod(String v){method=v;} public void setPath(String v){path=v;} public void setRequestType(String v){requestType=v;}
 public void setStatusCode(int v){statusCode=v;} public void setDurationMs(long v){durationMs=v;} public void setIpAddress(String v){ipAddress=v;}
 public void setCountry(String v){country=v;} public void setUserAgent(String v){userAgent=v;}
}
