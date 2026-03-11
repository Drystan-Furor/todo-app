package com.example.todo.auth;

import com.example.todo.common.ApiException;
import com.example.todo.user.User;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long refreshExpirationMillis;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            Clock clock,
            @Value("${app.jwt.refresh-expiration}") long refreshExpirationMillis
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
        this.refreshExpirationMillis = refreshExpirationMillis;
    }

    @Transactional
    public IssuedRefreshToken issue(User user) {
        String plainToken = generateTokenValue();
        RefreshToken refreshToken = new RefreshToken(
                user,
                RefreshToken.hashToken(plainToken),
                Instant.now(clock).plusMillis(refreshExpirationMillis)
        );
        refreshTokenRepository.save(refreshToken);
        return new IssuedRefreshToken(plainToken, refreshToken);
    }

    @Transactional
    public IssuedRefreshToken rotate(String token) {
        RefreshToken currentToken = refreshTokenRepository.findByTokenHash(RefreshToken.hashToken(token))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        Instant now = Instant.now(clock);
        if (!currentToken.isActive(now)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token is expired or revoked");
        }

        String replacementValue = generateTokenValue();
        String replacementHash = RefreshToken.hashToken(replacementValue);
        currentToken.revoke(replacementHash, now);

        RefreshToken replacementToken = new RefreshToken(
                currentToken.getUser(),
                replacementHash,
                now.plusMillis(refreshExpirationMillis)
        );
        refreshTokenRepository.save(replacementToken);

        return new IssuedRefreshToken(replacementValue, replacementToken);
    }

    private String generateTokenValue() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record IssuedRefreshToken(String value, RefreshToken token) {
    }
}
