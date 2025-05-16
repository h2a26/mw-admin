package org.hein.service;

import org.hein.api.input.auth.UserRegistrationRequest;
import org.hein.api.output.auth.UserRegistrationResponse;

public interface AuthService {
    UserRegistrationResponse registerUser(UserRegistrationRequest userRegistrationRequest);
}
