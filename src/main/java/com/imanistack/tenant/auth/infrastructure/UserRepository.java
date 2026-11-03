package com.imanistack.tenant.auth.infrastructure;

import com.imanistack.tenant.auth.domain.UserType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmailAndTenantId(String email, UUID tenantId);
    Boolean existsByIdAndTenantIdAndRole(UUID id, UUID tenantId, UserType role);
}