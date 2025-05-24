package org.hein.service;


import org.hein.api.request.role.RoleRequest;
import org.hein.api.response.role.RoleResponse;

import java.util.List;

public interface RoleService {
    RoleResponse create(RoleRequest request);
    RoleResponse update(Long id, RoleRequest request);
    List<RoleResponse> findAll();
    RoleResponse findById(Long id);
    void deleteById(Long id);
}
