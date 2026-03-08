package com.example.todo.auth;

import com.example.todo.auth.dto.AuthRequest;
import com.example.todo.auth.dto.AuthResponse;
import com.example.todo.auth.dto.AuthUserResponse;
import com.example.todo.common.ApiException;
import com.example.todo.user.User;
import com.example.todo.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(AuthRequest request) {
        userRepository.findByEmail(request.email())
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Email is already registered");
                });

        User user = new User(request.email(), passwordEncoder.encode(request.password()));
        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        AuthUserResponse userResponse = new AuthUserResponse(user.getId(), user.getEmail(), user.getCreatedAt());
        return new AuthResponse(token, "Bearer", userResponse);
    }
}
