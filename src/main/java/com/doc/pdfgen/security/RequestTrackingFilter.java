package com.doc.pdfgen.security;

import com.doc.pdfgen.persistence.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Component
public class RequestTrackingFilter extends OncePerRequestFilter {
 private final RequestEventRepository repository; private final String statsServiceKey; public RequestTrackingFilter(RequestEventRepository r,@Value("${stats.service-key:}")String statsServiceKey){repository=r;this.statsServiceKey=statsServiceKey;}
 @Override protected boolean shouldNotFilter(HttpServletRequest r){String path=r.getRequestURI();return path.equals("/actuator/health")||path.equals("/api/stats")||path.startsWith("/api/stats/")||isAdminDevice(r);}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  long start=System.nanoTime(); try{chain.doFilter(req,res);}finally{try{
   RequestEventEntity e=new RequestEventEntity(); Authentication a=SecurityContextHolder.getContext().getAuthentication();
   if(a!=null&&a.getPrincipal() instanceof TenantPrincipal p){e.setTenantId(p.tenantId());e.setUserId(p.userId());}
   e.setOccurredAt(Instant.now());e.setMethod(req.getMethod());e.setPath(limit(first(req.getHeader("X-Original-Path"),req.getRequestURI()),300));e.setRequestType(type(req));e.setStatusCode(res.getStatus());e.setDurationMs((System.nanoTime()-start)/1_000_000);
   e.setIpAddress(limit(first(req.getHeader("CF-Connecting-IP"),req.getHeader("X-Forwarded-For"),req.getRemoteAddr()),64));
   String country=req.getHeader("CF-IPCountry");e.setCountry(country!=null&&country.matches("[A-Za-z]{2}")?country.toUpperCase():"XX");e.setUserAgent(limit(req.getHeader("User-Agent"),300));
   e.setReferer(limit(firstNullable(req.getHeader("X-Original-Referer"),req.getHeader("Referer")),500));
   e.setVisitorId(safeId(req.getHeader("X-Visitor-Id")));e.setSessionId(safeId(req.getHeader("X-Session-Id")));
   e.setScreenWidth(number(req.getHeader("X-Screen-Width"),10000));e.setScreenHeight(number(req.getHeader("X-Screen-Height"),10000));
   e.setTimeOnPageMs(longNumber(req.getHeader("X-Time-On-Page-Ms"),86_400_000L));e.setPreviousPath(limit(req.getHeader("X-Previous-Path"),300));
   e.setNextAction(limit(req.getHeader("X-Next-Action"),160));String eventType=type(req);String tool=limit(req.getHeader("X-Tool-Name"),80);
   if(tool==null&&eventType.equals("PDF_TOOL")){String path=req.getRequestURI();tool=limit(path.substring(path.lastIndexOf('/')+1),80);}if(tool==null&&eventType.equals("WORKFLOW_RUN"))tool="workflow";e.setToolName(tool);
   String mode=upper(req.getHeader("X-Processing-Mode"));if(mode==null&&(eventType.equals("PDF_TOOL")||eventType.equals("WORKFLOW_RUN")))mode="SERVER";e.setProcessingMode(mode);
   boolean successful=res.getStatus()>=200&&res.getStatus()<400;e.setToolUsed((successful&&(eventType.equals("PDF_TOOL")||eventType.equals("WORKFLOW_RUN")))||"true".equalsIgnoreCase(req.getHeader("X-Tool-Used")));repository.save(e);
  }catch(Exception ignored){logger.warn("Unable to record request event",ignored);}}
 }
 private String type(HttpServletRequest r){String p=r.getRequestURI();if(p.contains("/tracking/")){String event=upper(r.getHeader("X-Event-Type"));return event!=null&&event.matches("PAGE_VIEW|FILE_SELECTED|TOOL_USED|DOWNLOAD|PAGE_LEAVE|ACTION")?event:"PAGE_VIEW";}if(p.contains("/workflows/run"))return"WORKFLOW_RUN";if(p.contains("/workflows"))return"WORKFLOW_DEFINITION";if(p.contains("/auth/"))return"AUTH";if(p.contains("/pdf/"))return"PDF_TOOL";return"OTHER";}
 private boolean isAdminDevice(HttpServletRequest r){String supplied=r.getHeader("X-Admin-Device-Key");return !statsServiceKey.isBlank()&&supplied!=null&&MessageDigest.isEqual(statsServiceKey.getBytes(StandardCharsets.UTF_8),supplied.getBytes(StandardCharsets.UTF_8));}
 private String first(String... values){for(String v:values)if(v!=null&&!v.isBlank())return v.split(",")[0].trim();return"unknown";} private String limit(String v,int n){return v==null?null:v.substring(0,Math.min(n,v.length()));}
 private String firstNullable(String...values){for(String v:values)if(v!=null&&!v.isBlank())return v;return null;}
 private String upper(String v){return v==null||v.isBlank()?null:limit(v.trim().toUpperCase(),20);}
 private String safeId(String v){return v!=null&&v.matches("[A-Za-z0-9_-]{8,64}")?v:null;}
 private Integer number(String v,int max){try{int n=Integer.parseInt(v);return n>=0&&n<=max?n:null;}catch(Exception ignored){return null;}}
 private Long longNumber(String v,long max){try{long n=Long.parseLong(v);return n>=0&&n<=max?n:null;}catch(Exception ignored){return null;}}
}
