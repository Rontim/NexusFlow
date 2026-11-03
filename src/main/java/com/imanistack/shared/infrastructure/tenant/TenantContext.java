package com.imanistack.shared.infrastructure.tenant;

import com.imanistack.shared.infrastructure.exception.InvalidTenantContextException;

import java.util.UUID;

public class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {}

    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static UUID getCurrentTenantId() {
        String tenantId = CURRENT_TENANT.get();
        if (tenantId == null || tenantId.isBlank()) {
            throw new InvalidTenantContextException("The X-Tenant-ID header is required");
        }

        try {
            return UUID.fromString(tenantId);
        } catch (IllegalArgumentException exception) {
            throw new InvalidTenantContextException("The X-Tenant-ID header must be a UUID");
        }
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
