package org.example.service;

import org.example.dto.auth.JwtRequest;
import org.example.dto.auth.JwtResponse;

public interface AuthService {
    JwtResponse login(JwtRequest jwtRequest);

}
