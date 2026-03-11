package com.example.todo.auth;

import com.example.todo.auth.dto.AuthRequest;
import com.example.todo.auth.dto.AuthResponse;
import com.example.todo.auth.dto.AuthUserResponse;
import com.example.todo.auth.dto.RefreshTokenRequest;
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
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
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

    @Transactional
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.rotate(request.refreshToken());
        return buildAuthResponse(refreshToken.token().getUser(), refreshToken);
    }

    private AuthResponse buildAuthResponse(User user) {
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.issue(user);
        return buildAuthResponse(user, refreshToken);
    }

    private AuthResponse buildAuthResponse(User user, RefreshTokenService.IssuedRefreshToken refreshToken) {
        String accessToken = jwtService.generateToken(user);
        AuthUserResponse userResponse = new AuthUserResponse(user.getId(), user.getEmail(), user.getCreatedAt());
        return new AuthResponse(accessToken, accessToken, refreshToken.value(), "Bearer", userResponse);
    }
}
