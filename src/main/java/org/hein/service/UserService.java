package org.hein.service;

import org.hein.entity.User;

import org.hein.api.input.user.UserRequest;
import org.hein.api.output.user.UserResponse;

import java.util.List;

public interface UserService {
    User findByEmail(String email);
    UserResponse create(UserRequest request);
    UserResponse update(Long id, UserRequest request);
    void delete(Long id);
    UserResponse getById(Long id);
    List<UserResponse> getAll();
}
