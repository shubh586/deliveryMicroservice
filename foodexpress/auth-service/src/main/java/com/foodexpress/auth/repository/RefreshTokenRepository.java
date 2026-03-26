package com.foodexpress.auth.repository;

import com.foodexpress.auth.model.entity.RefreshToken;
import com.foodexpress.auth.model.entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RefreshTokenRepository {
    Optional<RefreshToken> findByRefreshToken(String token);
    @Modifying
    @Query("UPDATE RefreshToken rt set rt.revoked=false where rt.user=:user")
    void revokeAllByUser(User user);
}
