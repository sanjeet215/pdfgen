package com.doc.pdfgen.persistence;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public class IdentityRepository {
    private final EntityManager em;
    public IdentityRepository(EntityManager em){this.em=em;}
    public Optional<AppUserEntity> findUserByEmail(String email){
        return em.createQuery("select u from AppUserEntity u where lower(u.email)=:email", AppUserEntity.class)
                .setParameter("email", email.toLowerCase()).getResultStream().findFirst();
    }
    public TenantEntity findTenant(long id){return em.find(TenantEntity.class,id);}
    @Transactional public Registration saveTenantAndUser(TenantEntity tenant, AppUserEntity user){
        em.persist(tenant); em.flush(); user.setTenantId(tenant.getId()); em.persist(user); return new Registration(tenant,user);
    }
    public record Registration(TenantEntity tenant, AppUserEntity user){}
}
