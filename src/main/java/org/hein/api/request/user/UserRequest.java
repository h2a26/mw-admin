package org.hein.api.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Collections;
import java.util.Set;

/**
 * Request DTO for updating an existing user
 */
/**
 * Request DTO for updating an existing user
 */
public record UserRequest(
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        String firstName,
        
        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        String lastName,
        
        @Email(message = "Invalid email format")
        @Size(max = 100, message = "Email cannot exceed 100 characters")
        String email,
        
        @Size(max = 20, message = "Mobile phone cannot exceed 20 characters")
        String mobilePhone,
        
        String password,
        
        Boolean twoFactorEnabled,
        
        Boolean enabled,
        
        Boolean locked,
        
        Boolean systemAccount,
        
        Set<Long> roleIds,

        Integer passwordExpiryDays
) {
    /**
     * Get the role IDs, returning an empty set if null
     */
    public Set<Long> getRoleIds() {
        return this.roleIds != null ? this.roleIds : Collections.emptySet();
    }

}
