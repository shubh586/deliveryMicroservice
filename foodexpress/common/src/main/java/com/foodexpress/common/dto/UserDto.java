package com.foodexpress.common.dto;

import com.foodexpress.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * User DTO for inter-service communication
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String email;
    private String name;
    private String phone;
    private String profilePicture;
    private Role role;
    private Boolean enabled;
    private Instant createdAt;
}
