package org.hein.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hein.entity.Permission;
import org.hein.entity.User;
import org.hein.service.UserService;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service to load user-specific data for Spring Security.
 * This service adapts our User entity to Spring Security's UserDetails interface.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

	private final UserService userService;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.debug("Authenticating {}", username);
		
		User user = userService.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException("User '" + username + "' not found");
		}

		if (!user.isEnabled()) {
			throw new DisabledException("User is disabled");
		}

		if (user.isLocked()) {
			throw new LockedException("User is locked");
		}

		return org.springframework.security.core.userdetails.User
				.builder()
				.username(user.getUsername())
				.password(user.getPassword())
				.authorities(getAuthorities(user))
				.accountExpired(false)
				.accountLocked(user.isLocked())
				.credentialsExpired(user.isPasswordExpired())
				.disabled(!user.isEnabled())
				.build();
	}

	/**
	 * Extract all permissions from a user's roles and direct permissions,
	 * and convert them to Spring Security GrantedAuthorities.
	 * 
	 * @param user The user whose permissions to extract
	 * @return A set of GrantedAuthority objects representing the user's permissions
	 */
	private Set<GrantedAuthority> getAuthorities(User user) {
		// Get all permissions from the user (including from roles and direct permissions)
		Set<Permission> allPermissions = user.getPermissions();
		
		// Convert each permission to a GrantedAuthority using the format "feature:ACTION"
		Set<GrantedAuthority> authorities = allPermissions.stream()
			.map(permission -> new SimpleGrantedAuthority(
				permission.getPermissionName()))
			.collect(Collectors.toSet());
		
		// Also add role-based authorities for role-based checks
		user.getRoles().forEach(role -> {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()));
		});
		
		return authorities;
	}
}
