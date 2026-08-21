package com.doc.pdfgen.security;

import org.springframework.security.core.context.SecurityContextHolder;

public final class TenantContext {
    private TenantContext(){}
    public static TenantPrincipal current(){
        Object principal=SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal instanceof TenantPrincipal tenant) return tenant;
        throw new IllegalStateException("Authenticated tenant is required");
    }
}
