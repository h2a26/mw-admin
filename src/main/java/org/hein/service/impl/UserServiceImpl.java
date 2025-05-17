package org.hein.service.impl;

import jakarta.transaction.Transactional;
import org.hein.api.input.user.UserRequest;
import org.hein.api.output.user.UserResponse;
import org.hein.entity.Role;
import org.hein.entity.User;
import org.hein.entity.UserRole;
import org.hein.repository.RoleRepository;
import org.hein.repository.UserRepository;
import org.hein.service.UserService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid email: " + email));
    }

    @Override
    public UserResponse create(UserRequest request) {
        Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.roleIds()));

        if (roles.size() != request.roleIds().size()) {
            throw new IllegalArgumentException("One or more role IDs are invalid.");
        }

        User user = User.builder()
                .username(request.username())
                .password(request.password())
                .email(request.email())
                .build();

        roles.forEach(user::addRole);

        return UserResponse.from(userRepository.save(user));
    }

    @Override
    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.roleIds()));
        if (roles.size() != request.roleIds().size()) {
            throw new IllegalArgumentException("One or more role IDs are invalid.");
        }

        user.setUsername(request.username());
        user.setPassword(request.password()); // TODO: encode password
        user.setEmail(request.email());

        // Clear and reassign roles
        user.clearRoles();
        roles.forEach(user::addRole); // reassign via helper

        return UserResponse.from(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserResponse getById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    @Override
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }
}
