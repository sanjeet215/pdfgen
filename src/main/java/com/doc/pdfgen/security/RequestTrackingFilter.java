package com.doc.pdfgen.security;

import com.doc.pdfgen.persistence.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;

@Component
public class RequestTrackingFilter extends OncePerRequestFilter {
 private final RequestEventRepository repository; public RequestTrackingFilter(RequestEventRepository r){repository=r;}
 @Override protected boolean shouldNotFilter(HttpServletRequest r){return r.getRequestURI().equals("/actuator/health");}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  long start=System.nanoTime(); try{chain.doFilter(req,res);}finally{try{
   RequestEventEntity e=new RequestEventEntity(); Authentication a=SecurityContextHolder.getContext().getAuthentication();
   if(a!=null&&a.getPrincipal() instanceof TenantPrincipal p){e.setTenantId(p.tenantId());e.setUserId(p.userId());}
   e.setOccurredAt(Instant.now());e.setMethod(req.getMethod());e.setPath(limit(first(req.getHeader("X-Original-Path"),req.getRequestURI()),300));e.setRequestType(type(req));e.setStatusCode(res.getStatus());e.setDurationMs((System.nanoTime()-start)/1_000_000);
   e.setIpAddress(limit(first(req.getHeader("CF-Connecting-IP"),req.getHeader("X-Forwarded-For"),req.getRemoteAddr()),64));
   String country=req.getHeader("CF-IPCountry");e.setCountry(country!=null&&country.matches("[A-Za-z]{2}")?country.toUpperCase():"XX");e.setUserAgent(limit(req.getHeader("User-Agent"),300));repository.save(e);
  }catch(Exception ignored){logger.warn("Unable to record request event",ignored);}}
 }
 private String type(HttpServletRequest r){String p=r.getRequestURI();if(p.contains("/tracking/page-view"))return"PAGE_VIEW";if(p.contains("/workflows/run"))return"WORKFLOW_RUN";if(p.contains("/workflows"))return"WORKFLOW_DEFINITION";if(p.contains("/auth/"))return"AUTH";if(p.contains("/pdf/"))return"PDF_TOOL";return"OTHER";}
 private String first(String... values){for(String v:values)if(v!=null&&!v.isBlank())return v.split(",")[0].trim();return"unknown";} private String limit(String v,int n){return v==null?null:v.substring(0,Math.min(n,v.length()));}
}
