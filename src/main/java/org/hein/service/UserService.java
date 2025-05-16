package org.hein.service;

import org.hein.entity.User;

public interface UserService {
    User findByEmail(String email);
    User save(User user);
}
