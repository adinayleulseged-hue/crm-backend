package com.crm.auth.repository;

import com.crm.auth.model.AuthUser;
import com.crm.auth.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<AuthUser> findByRoleAndEnabledTrue(UserRole role);

    List<AuthUser> findByTerritoryIdAndEnabledTrue(Long territoryId);

    List<AuthUser> findByRoleAndTerritoryIdAndEnabledTrue(UserRole role, Long territoryId);

    List<AuthUser> findByEnabledTrue();
}
