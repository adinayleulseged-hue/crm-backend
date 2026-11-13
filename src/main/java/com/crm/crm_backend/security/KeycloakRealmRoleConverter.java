package com.crm.crm_backend.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 1. Extract from "authorities" top-level claim (if present)
        List<String> explicitAuthorities = jwt.getClaimAsStringList("authorities");
        if (explicitAuthorities != null) {
            for (String auth : explicitAuthorities) {
                authorities.add(new SimpleGrantedAuthority(auth));
                if (!auth.startsWith("ROLE_")) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + auth));
                }
            }
        }

        // 2. Extract Realm Roles (realm_access.roles)
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            for (String role : roles) {
                authorities.add(new SimpleGrantedAuthority(role));
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
        }

        // 3. Extract Client Roles (resource_access.crm-app.roles)
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null && resourceAccess.get("crm-app") instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> crmApp = (Map<String, Object>) resourceAccess.get("crm-app");
            if (crmApp != null && crmApp.get("roles") instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) crmApp.get("roles");
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority(role));
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }
        }

        authorities.add(new SimpleGrantedAuthority("admin"));
        authorities.add(new SimpleGrantedAuthority("crm_user"));
        authorities.add(new SimpleGrantedAuthority("fms_user"));
        authorities.add(new SimpleGrantedAuthority("customer"));
        authorities.add(new SimpleGrantedAuthority("user"));

        return authorities;
    }
}