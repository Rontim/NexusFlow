package com.imanistack.tenant.auth.presentation.dtos;

import com.imanistack.tenant.auth.domain.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {
    public record  RegisterTenantRequest(
            @NotBlank(message = "Tenant name is required") String tenantName,
            @NotBlank(message = "Subdomain is required") String subdomain,
            @Email(message = "Invalid email format") @NotBlank String adminEmail,
            @Size(min = 8, message = "Password must be at least 8 characters") String adminPassword
    ) {}

    public record LoginRequest(
            @NotBlank(message = "Subdomain is required") String subdomain,
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(
            String token,
            String email,
            UserType role,
            String tenantId
    ) {}
}
