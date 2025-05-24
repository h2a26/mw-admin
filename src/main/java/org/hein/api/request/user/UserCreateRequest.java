package org.hein.api.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hein.entity.User;

import java.util.Collections;
import java.util.Set;

/**
 * Request DTO for creating a new user
 */
@Builder
public record UserCreateRequest(
        @NotBlank(message = "Username is required")
        @Pattern(regexp = "^[a-z0-9_]+$", message = "Username must contain only lowercase letters, numbers, and underscores")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,
        
        @NotBlank(message = "First name is required")
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        String firstName,
        
        @NotBlank(message = "Last name is required")
        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        String lastName,
        
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 100, message = "Email cannot exceed 100 characters")
        String email,
        
        @Size(max = 20, message = "Mobile phone cannot exceed 20 characters")
        String mobilePhone,
        
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,
        
        Boolean twoFactorEnabled,
        
        Boolean systemAccount,
        
        Set<Long> roleIds,
        
        Set<Long> directPermissionIds,
        
        Integer passwordExpiryDays
) {
    /**
     * Convert request DTO to entity
     * Note: roles and permissions need to be set separately
     */
    public User toEntity() {
        return User.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .mobilePhone(mobilePhone)
                .password(password) // Will be encoded in service
                .twoFactorEnabled(twoFactorEnabled != null ? twoFactorEnabled : false)
                .systemAccount(systemAccount != null ? systemAccount : false)
                .enabled(true)
                .locked(false)
                .build();
    }

    /**
     * Get the role IDs, returning an empty set if null
     */
    public Set<Long> getRoleIds() {
        return this.roleIds != null ? this.roleIds : Collections.emptySet();
    }
    
    /**
     * Get the direct permission IDs, returning an empty set if null
     */
    public Set<Long> getDirectPermissionIds() {
        return this.directPermissionIds != null ? this.directPermissionIds : Collections.emptySet();
    }
}