package com.foodexpress.auth.repository;

import com.foodexpress.auth.model.Dto.AuthResponse;
import com.foodexpress.auth.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String id);
    boolean existsByEmail(String email);

    AuthResponse.UserInfo getUserById(String userId);
}
