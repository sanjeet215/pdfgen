package com.doc.pdfgen.security;

import com.doc.pdfgen.persistence.AppUserEntity;
import com.doc.pdfgen.persistence.IdentityRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class TenantUserDetailsService implements UserDetailsService {
    private final IdentityRepository repository;
    public TenantUserDetailsService(IdentityRepository repository){this.repository=repository;}
    @Override public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUserEntity user=repository.findUserByEmail(email).orElseThrow(()->new UsernameNotFoundException("Invalid credentials"));
        return new TenantPrincipal(user.getId(),user.getTenantId(),user.getDisplayName(),user.getEmail(),user.getPasswordHash(),user.getRole(),user.isEnabled());
    }
}
