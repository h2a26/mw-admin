package org.hein.service;

import org.hein.api.request.user.UserRequest;
import org.hein.api.response.user.UserResponse;
import org.hein.entity.User;


import java.util.List;

public interface UserService {
    User findByUsername(String username);
    UserResponse create(UserRequest request);
    UserResponse update(Long id, UserRequest request);
    void deleteById(Long id);
    UserResponse getById(Long id);
    List<UserResponse> getAll();
}
