package com.imanistack.tenant.auth.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<TenantEntity, UUID> {
    Optional<TenantEntity> findBySubdomain(String subdomain);
}
