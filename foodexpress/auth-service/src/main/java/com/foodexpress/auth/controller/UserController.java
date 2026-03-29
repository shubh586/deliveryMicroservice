package com.foodexpress.auth.controller;
import com.foodexpress.auth.model.Dto.AuthRequest;
import com.foodexpress.auth.model.Dto.AuthResponse;
import com.foodexpress.auth.repository.UserRepository;
import com.foodexpress.auth.service.UserService;
import com.foodexpress.common.dto.ApiResponse;
import com.foodexpress.common.dto.PagedResponse;
import com.foodexpress.common.enums.Role;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name=" user controller" , description = "fetching the current use details")
public class UserController {
    private final UserService userService;

    @GetMapping("/user/me")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>>  getCurrentUser(@RequestHeader ("X-User-Id") String userId) {
        AuthResponse.UserInfo user =userService.getCurrentUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(user));
    }

    @Operation(summary = "Update current user profile")
    @PutMapping("/users/me")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> updateProfile(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone
    ){
        AuthResponse.UserInfo user =userService.updateProfile(userId,name,phone);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(user));
    }
    @Operation(summary = "Changing the users password")
    @PutMapping("/users/change")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> changePassword(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AuthRequest.ChangePassword request
            ){
            userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<AuthResponse.UserInfo>>> getAllUsers(
            @PageableDefault(size = 10) Pageable pageable
    ){
        Page<AuthResponse.UserInfo> pagedResponse=userService.getAllUsers(pageable);
        PagedResponse<AuthResponse.UserInfo> userInfoPagedResponse=PagedResponse.<AuthResponse.UserInfo>builder()
                .content(pagedResponse.getContent())
                .page(pagedResponse.getNumber())
                .size(pagedResponse.getSize())
                .totalElements(pagedResponse.getTotalElements())
                .totalPages(pagedResponse.getTotalPages())
                .first(pagedResponse.isFirst())
                .last(pagedResponse.isLast())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(userInfoPagedResponse));

    }


    @PutMapping("/admin/users/{userId}/role")
    @PreAuthorize("hasRole ('ADMIN')")
    @Operation(summary = "updating users role")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> updateRole(
            @RequestParam Role role,
            @PathVariable String userId){
        AuthResponse.UserInfo userInfo=userService.changeUserRole(userId,role);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(userInfo,"Role changed successfully"));
    }


    @PutMapping("/admin/users/{userId}/toggle-enabled")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable/Disable user (Admin only)")
    public ResponseEntity<ApiResponse<Void>> toggleUserEnabled(@PathVariable String userId) {

        userService.toggleUserEnabled(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User status updated successfully"));
    }
}
