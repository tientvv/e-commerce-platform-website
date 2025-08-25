package com.tientvv.repository;

import com.tientvv.model.ResetPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, UUID> {
    
    Optional<ResetPasswordToken> findByToken(String token);
    
    @Query("SELECT rpt FROM ResetPasswordToken rpt WHERE rpt.account.id = :accountId AND rpt.isUsed = false AND rpt.expiryDate > :now ORDER BY rpt.createdAt DESC")
    Optional<ResetPasswordToken> findValidTokenByAccountId(@Param("accountId") UUID accountId, @Param("now") OffsetDateTime now);
    
    @Query("SELECT COUNT(rpt) FROM ResetPasswordToken rpt WHERE rpt.account.id = :accountId AND rpt.createdAt > :since")
    long countRecentTokensByAccountId(@Param("accountId") UUID accountId, @Param("since") OffsetDateTime since);
}
