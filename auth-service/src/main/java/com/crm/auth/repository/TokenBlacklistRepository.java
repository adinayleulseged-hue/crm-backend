package com.crm.auth.repository;

import com.crm.auth.model.TokenBlacklistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklistEntry, String> {

    boolean existsByJtiAndExpiresAtAfter(String jti, LocalDateTime now);

    @Modifying
    @Query("delete from TokenBlacklistEntry e where e.expiresAt < ?1")
    int deleteExpired(LocalDateTime now);
}
