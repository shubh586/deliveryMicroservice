package com.foodexpress.auth.service;
import com.foodexpress.auth.model.Dto.AuthResponse;
import com.foodexpress.auth.model.entity.User;
import com.foodexpress.auth.repository.UserRepository;
import com.foodexpress.common.enums.Role;
import com.foodexpress.common.exception.BadRequestException;
import com.foodexpress.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
//  user event producer here we go

    @Transactional(readOnly = true)
    public AuthResponse.UserInfo getCurrentUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return toUserInfo(user);
    }

    @Transactional
    public AuthResponse.UserInfo updateProfile(String userId, String name, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (name != null) {
            user.setName(name);
        }
        if (phone != null) {
            user.setPhone(phone);
        }

        user = userRepository.save(user);
        logger.info("User profile updated: {}", userId);

        return toUserInfo(user);
    }

    @Transactional
    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getPassword() == null) {
            throw new BadRequestException("Cannot change password for OAuth2 user");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("Password changed for user: {}", userId);
    }

    // Admin methods
    @Transactional(readOnly = true)
    public Page<AuthResponse.UserInfo> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserInfo);
    }

    @Transactional
    public AuthResponse.UserInfo changeUserRole(String userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String previousRole = user.getRole().name();
        user.setRole(newRole);
        user = userRepository.save(user);

        logger.info("User role changed: userId={}, from={}, to={}", userId, previousRole, newRole);

        // publish role changed event remaining

        return toUserInfo(user);
    }

    @Transactional
    public void toggleUserEnabled(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setEnabled(!user.getEnabled());
        userRepository.save(user);

        logger.info("User enabled status toggled: userId={}, enabled={}", userId, user.getEnabled());
    }

    private AuthResponse.UserInfo toUserInfo(User user) {
        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .profilePicture(user.getProfilePicture())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

