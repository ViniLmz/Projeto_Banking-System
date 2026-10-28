
package com.bank_project.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public String login(LoginRequest request) {

        var authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                );

        var authentication =
                authenticationManager.authenticate(authenticationToken);

        return jwtService.generateToken(authentication.getName());
    }
}
