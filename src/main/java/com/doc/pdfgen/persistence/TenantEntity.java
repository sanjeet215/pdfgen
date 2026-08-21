package com.doc.pdfgen.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "tenants")
public class TenantEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, unique = true, length = 80) private String slug;
    @Column(nullable = false) private Instant createdAt;
    @PrePersist void beforeCreate() { createdAt = Instant.now(); }
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public Instant getCreatedAt() { return createdAt; }
}
