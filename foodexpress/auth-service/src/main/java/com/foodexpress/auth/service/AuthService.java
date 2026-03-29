package com.foodexpress.auth.service;


import com.foodexpress.auth.model.Dto.AuthRequest;
import com.foodexpress.auth.model.Dto.AuthResponse;
import com.foodexpress.auth.model.entity.RefreshToken;
import com.foodexpress.auth.model.entity.User;
import com.foodexpress.auth.repository.RefreshTokenRepository;
import com.foodexpress.auth.repository.UserRepository;
import com.foodexpress.auth.security.jwt.JwtTokenProvider;
import com.foodexpress.common.enums.Role;
import com.foodexpress.common.exception.BadRequestException;
import com.foodexpress.common.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger= LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;


    //public AuthResponse.TokenResponse register(AuthRequest.Register request)

    @Transactional
    public AuthResponse.TokenResponse  register(AuthRequest.Register request){
        logger.info("Register request received , new user: {}",request.getEmail());
        if(userRepository.existsByEmail((request.getEmail()))){
            throw new BadRequestException("Email already exists");
        }
        User user= User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .name(request.getName())
                .role(request.getRole() != null ? request.getRole() : Role.ROLE_CUSTOMER)
                .provider("LOCAL")
                .enabled(true)
                .emailVerified(false)
                .build();
        user = userRepository.save(user);
        logger.info("Register request saved new new user: {}",user.getEmail());
        /// usrevents . register event here
        return  generateTokenResponse(user) ;
    }

    @Transactional
    public AuthResponse.TokenResponse login(AuthRequest.Login request){
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid email or password");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        if (!user.getEnabled()) {
            throw new UnauthorizedException("Account is disabled");
        }
        logger.info("User logged in successfully: {}", user.getId());
        return generateTokenResponse(user);
    }

    @Transactional
    public AuthResponse.TokenResponse refreshToken(AuthRequest.RefreshToken request) {
        logger.info("Refreshing token");
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (!refreshToken.isValid()) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }
        User user = refreshToken.getUser();
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        logger.info("Token refreshed for user: {}", user.getId());
        return generateTokenResponse(user);
    }

    @Transactional
    public void logout(String userId) {
        logger.info("Logging out user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        refreshTokenRepository.revokeAllByUser(user);
        logger.info("User logged out: {}", userId);
    }




    private AuthResponse.TokenResponse generateTokenResponse(User user){
        String accessToken= jwtTokenProvider.generateAccessToken(user.getId(),user.getEmail(),user.getRole().name());
        String refreshToken= jwtTokenProvider.generateRefreshToken();
        RefreshToken refreshToken1=RefreshToken.builder()
                .token(refreshToken)
                .expiresAt(jwtTokenProvider.getRefreshTokenExpiry())
                .user(user)
                .build();
        refreshTokenRepository.save(refreshToken1);
        return AuthResponse.TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L) // 24 hours in seconds
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .phone(user.getPhone())
                        .profilePicture(user.getProfilePicture())
                        .role(user.getRole())
                        .createdAt(user.getCreatedAt())
                        .build())
                .build();
    }

    }
