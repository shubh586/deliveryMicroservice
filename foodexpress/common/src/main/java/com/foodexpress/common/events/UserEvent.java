package com.foodexpress.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event published when user registers or updates profile
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserEvent extends BaseEvent {

    public static final String TOPIC = "user-events";

    public static final String USER_REGISTERED = "USER_REGISTERED";
    public static final String USER_UPDATED = "USER_UPDATED";
    public static final String USER_DELETED = "USER_DELETED";
    public static final String ROLE_CHANGED = "ROLE_CHANGED";

    private String userId;
    private String email;
    private String name;
    private String phone;
    private String role;
    private String previousRole;
    private Boolean enabled;
}
