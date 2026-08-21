package com.doc.pdfgen.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "app_users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class AppUserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="tenant_id", nullable=false) private Long tenantId;
    @Column(nullable=false, length=254) private String email;
    @Column(nullable=false, length=100) private String displayName;
    @Column(nullable=false, length=255) private String passwordHash;
    @Column(nullable=false, length=30) private String role;
    @Column(nullable=false) private boolean enabled = true;
    @Column(nullable=false) private Instant createdAt;
    @PrePersist void beforeCreate() { createdAt = Instant.now(); }
    public Long getId(){return id;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getDisplayName(){return displayName;}
    public void setDisplayName(String v){displayName=v;} public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String v){passwordHash=v;}
    public String getRole(){return role;} public void setRole(String v){role=v;} public boolean isEnabled(){return enabled;} public Instant getCreatedAt(){return createdAt;}
}
