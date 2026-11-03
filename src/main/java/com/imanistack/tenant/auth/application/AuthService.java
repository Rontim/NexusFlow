package com.imanistack.tenant.auth.application;

import com.imanistack.shared.infrastructure.exception.InvalidCredentialsException;
import com.imanistack.shared.infrastructure.exception.ResourceNotFoundException;
import com.imanistack.shared.infrastructure.security.JwtTokenProvider;
import com.imanistack.tenant.auth.domain.UserType;
import com.imanistack.tenant.auth.infrastructure.TenantEntity;
import com.imanistack.tenant.auth.infrastructure.TenantRepository;
import com.imanistack.tenant.auth.infrastructure.UserEntity;
import com.imanistack.tenant.auth.infrastructure.UserRepository;
import com.imanistack.tenant.auth.presentation.dtos.AuthDtos.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(TenantRepository tenantRepository,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse registerTenant(RegisterTenantRequest request) {
        TenantEntity tenant = new TenantEntity();
        tenant.setName(request.tenantName());
        tenant.setSubdomain(request.subdomain());
        TenantEntity savedTenant = tenantRepository.save(tenant);

        UserEntity tenantAdmin = new UserEntity();
        tenantAdmin.setTenantId(savedTenant.getId());
        tenantAdmin.setEmail(request.adminEmail());
        tenantAdmin.setPasswordHash(passwordEncoder.encode(request.adminPassword()));
        tenantAdmin.setRole(UserType.ADMIN);
        userRepository.save(tenantAdmin);

        String token = jwtTokenProvider.generateToken(tenantAdmin.getEmail(), savedTenant.getId().toString(), String.valueOf(tenantAdmin.getRole()));

        return new AuthResponse(token, tenantAdmin.getEmail(), tenantAdmin.getRole(), savedTenant.getId().toString());
    }

    public AuthResponse login(LoginRequest request) {
        TenantEntity tenant = tenantRepository.findBySubdomain(request.subdomain())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with subdomain: " + request.subdomain()));

        UserEntity user = userRepository.findByEmailAndTenantId(request.email(), tenant.getId())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), tenant.getId().toString(), String.valueOf(user.getRole()));
        return new AuthResponse(token, user.getEmail(), user.getRole(), tenant.getId().toString());
    }
}
