package org.hein.service.impl;

import lombok.RequiredArgsConstructor;
import org.hein.api.input.auth.UserRegistrationRequest;
import org.hein.api.output.auth.UserRegistrationResponse;
import org.hein.commons.enum_.RoleEnum;
import org.hein.entity.Role;
import org.hein.entity.User;
import org.hein.exceptions.ApiBusinessException;
import org.hein.repository.RoleRepository;
import org.hein.service.AuthService;
import org.hein.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserService userService;

    @Override
    public UserRegistrationResponse registerUser(UserRegistrationRequest userRegistrationRequest) {
        User candidate = userService.findByEmail(userRegistrationRequest.email());

        Role role = roleRepository.findByName(RoleEnum.USER.toString()).orElseThrow(() -> new ApiBusinessException("Role User not found"));

        User user = User.builder()
                .email(candidate.getEmail())
                .username(userRegistrationRequest.username())
                .password(passwordEncoder.encode(userRegistrationRequest.password()))
                .build();
        user.addRole(role);

        return UserRegistrationResponse.from(user);
    }
}
