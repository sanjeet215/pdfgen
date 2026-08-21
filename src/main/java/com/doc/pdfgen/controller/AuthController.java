package com.doc.pdfgen.controller;

import com.doc.pdfgen.persistence.*;
import com.doc.pdfgen.security.TenantContext;
import com.doc.pdfgen.security.TenantPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import java.util.Locale;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final IdentityRepository repository; private final PasswordEncoder encoder; private final AuthenticationManager manager;
    public AuthController(IdentityRepository r,PasswordEncoder e,AuthenticationManager m){repository=r;encoder=e;manager=m;}
    public record RegisterRequest(String tenantName,String displayName,String email,String password){}
    public record LoginRequest(String email,String password){}
    public record SessionResponse(Long userId,Long tenantId,String tenantName,String displayName,String email,String role){}
    @PostMapping("/register") public SessionResponse register(@RequestBody RegisterRequest body,HttpServletRequest request,HttpServletResponse response){
        if(body==null||blank(body.tenantName())||blank(body.displayName())||blank(body.email())||body.password()==null||body.password().length()<10) throw new IllegalArgumentException("Organization, name, valid email, and a password of at least 10 characters are required");
        if(repository.findUserByEmail(body.email()).isPresent()) throw new IllegalArgumentException("An account with this email already exists");
        TenantEntity tenant=new TenantEntity(); tenant.setName(body.tenantName().trim()); tenant.setSlug(slug(body.tenantName()));
        AppUserEntity user=new AppUserEntity(); user.setEmail(body.email().trim().toLowerCase()); user.setDisplayName(body.displayName().trim()); user.setPasswordHash(encoder.encode(body.password())); user.setRole("TENANT_ADMIN");
        repository.saveTenantAndUser(tenant,user); return authenticate(body.email(),body.password(),request,response);
    }
    @PostMapping("/login") public SessionResponse login(@RequestBody LoginRequest body,HttpServletRequest request,HttpServletResponse response){return authenticate(body.email(),body.password(),request,response);}
    @GetMapping("/me") public SessionResponse me(){TenantPrincipal p=TenantContext.current();return response(p);}
    @PostMapping("/logout") public ResponseEntity<Void> logout(HttpSession session){session.invalidate();return ResponseEntity.noContent().build();}
    private SessionResponse authenticate(String email,String password,HttpServletRequest request,HttpServletResponse response){
        var auth=manager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email,password));
        SecurityContext context=SecurityContextHolder.createEmptyContext(); context.setAuthentication(auth); SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context,request,response); return response((TenantPrincipal)auth.getPrincipal());
    }
    private SessionResponse response(TenantPrincipal p){TenantEntity t=repository.findTenant(p.tenantId());return new SessionResponse(p.userId(),p.tenantId(),t.getName(),p.displayName(),p.username(),p.role());}
    private boolean blank(String s){return s==null||s.isBlank();} private String slug(String s){String v=s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","-").replaceAll("(^-|-$)","");return (v.isBlank()?"tenant":v)+"-"+Long.toString(System.nanoTime(),36);}
}
