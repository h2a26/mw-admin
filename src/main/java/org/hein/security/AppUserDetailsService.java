package org.hein.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hein.entity.User;
import org.hein.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.debug("Authenticating {}", username);
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));

		if (!user.isEnabled()) {
			throw new DisabledException("User is disabled");
		}

		if (user.isLocked()) {
			throw new LockedException("User is locked");
		}

		return new org.springframework.security.core.userdetails.User(
				user.getUsername(),
				user.getPassword(),
				getAuthorities(user)
		);
	}

	private Set<GrantedAuthority> getAuthorities(User user) {
		return user.getAllRoles().stream()
				.flatMap(role -> role.getPermissions().stream())
				.map(permission -> {
					String featureName = permission.getFeature().getName();
					String actionName = permission.getAction().name();
					return new SimpleGrantedAuthority(featureName + "_" + actionName);
				})
				.collect(Collectors.toSet());
	}
}
