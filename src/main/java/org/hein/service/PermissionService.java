package org.hein.service;

import org.hein.api.input.permission.PermissionRequest;
import org.hein.api.output.permission.PermissionResponse;

import java.util.List;

public interface PermissionService {

    PermissionResponse create(PermissionRequest request);
    List<PermissionResponse> findAll();
    PermissionResponse findById(Long id);
    void deleteById(Long id);
}

