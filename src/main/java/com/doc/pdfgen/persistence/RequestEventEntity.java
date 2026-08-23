package com.doc.pdfgen.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="request_events", indexes={@Index(name="idx_request_event_time",columnList="occurredAt"),@Index(name="idx_request_event_tenant",columnList="tenantId"),@Index(name="idx_request_event_visitor",columnList="visitorId"),@Index(name="idx_request_event_session",columnList="sessionId")})
public class RequestEventEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long tenantId; private Long userId; @Column(nullable=false) private Instant occurredAt;
 @Column(nullable=false,length=10) private String method; @Column(nullable=false,length=300) private String path;
 @Column(nullable=false,length=80) private String requestType; @Column(nullable=false) private int statusCode;
 @Column(nullable=false) private long durationMs; @Column(length=64) private String ipAddress; @Column(length=2) private String country;
 @Column(length=300) private String userAgent;
 @Column(length=500) private String referer; @Column(length=64) private String visitorId; @Column(length=64) private String sessionId;
 private Integer screenWidth; private Integer screenHeight; private Long timeOnPageMs;
 @Column(length=300) private String previousPath; @Column(length=160) private String nextAction;
 @Column(length=80) private String toolName; @Column(length=20) private String processingMode; private boolean toolUsed;
 public void setTenantId(Long v){tenantId=v;} public void setUserId(Long v){userId=v;} public void setOccurredAt(Instant v){occurredAt=v;}
 public void setMethod(String v){method=v;} public void setPath(String v){path=v;} public void setRequestType(String v){requestType=v;}
 public void setStatusCode(int v){statusCode=v;} public void setDurationMs(long v){durationMs=v;} public void setIpAddress(String v){ipAddress=v;}
 public void setCountry(String v){country=v;} public void setUserAgent(String v){userAgent=v;}
 public void setReferer(String v){referer=v;} public void setVisitorId(String v){visitorId=v;} public void setSessionId(String v){sessionId=v;}
 public void setScreenWidth(Integer v){screenWidth=v;} public void setScreenHeight(Integer v){screenHeight=v;} public void setTimeOnPageMs(Long v){timeOnPageMs=v;}
 public void setPreviousPath(String v){previousPath=v;} public void setNextAction(String v){nextAction=v;} public void setToolName(String v){toolName=v;}
 public void setProcessingMode(String v){processingMode=v;} public void setToolUsed(boolean v){toolUsed=v;}
}
