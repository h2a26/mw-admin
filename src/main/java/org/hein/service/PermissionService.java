package org.hein.service;

import org.hein.api.request.permission.PermissionRequest;
import org.hein.api.response.permission.PermissionResponse;

import java.util.List;

public interface PermissionService {

    PermissionResponse create(PermissionRequest request);
    PermissionResponse update(Long id, PermissionRequest request);
    List<PermissionResponse> findAll();
    PermissionResponse findById(Long id);
    void deleteById(Long id);
}

