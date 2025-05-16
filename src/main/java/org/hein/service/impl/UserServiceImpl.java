package org.hein.service.impl;

import lombok.AllArgsConstructor;
import org.hein.entity.User;
import org.hein.repository.UserRepository;
import org.hein.service.UserService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid email: " + email));
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
}
