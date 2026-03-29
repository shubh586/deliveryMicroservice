package com.foodexpress.auth.controller;


import com.foodexpress.auth.model.Dto.AuthRequest;
import com.foodexpress.auth.model.Dto.AuthResponse;
import com.foodexpress.auth.service.AuthService;
import com.foodexpress.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {
    private final AuthService authService;


    @PostMapping("/register")
    @Operation(summary = "new user register")
    public ResponseEntity<ApiResponse<AuthResponse.TokenResponse>> register(
            @Valid @RequestBody AuthRequest.Register authRequest) {
        AuthResponse.TokenResponse response = authService.register(authRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response,"successfully register"));
    }


    @PostMapping("/login")
    @Operation(summary = "login new user")
    public ResponseEntity<ApiResponse<AuthResponse.TokenResponse>> login(
            @Valid @RequestBody AuthRequest.Login authRequest

    ){
        AuthResponse.TokenResponse response=authService.login(authRequest);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response,"successfully login"));
    }

    @PostMapping ("/logout")
    @Operation(summary = "user logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("X-User-Id") String userId
    ){
        authService.logout(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null,"successfully logout"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse.TokenResponse>> refreshToken(
            @Valid @RequestBody AuthRequest.RefreshToken request) {

        AuthResponse.TokenResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

}
