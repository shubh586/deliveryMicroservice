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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger= LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;


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
        return  ;
    }



    private AuthResponse.TokenResponse generateTokenResponse(User user){
        String accessToken= jwtTokenProvider.generateAccessToken(user.getId(),user.getEmail(),user.getRole().name());
        String refreshToken= jwtTokenProvider.generateRefreshToken();
        RefreshToken refreshToken1=RefreshToken.builder()
                .token(refreshToken)
                .expiresAt(jwtTokenProvider.getRefreshTokenExpiry())
                .user(user)
                .build();
        refreshTokenRepository.save(RefreshToken);

    }





}
