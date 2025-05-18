package org.hein.security.token;

import lombok.RequiredArgsConstructor;
import org.hein.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static org.hein.commons.constant.RedisKeys.REFRESH_TOKEN_BY_USER;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    @Value("${app.token.expiration.refresh}")
    private int refreshLife;

    private final RedisUtil redisUtil;

    public void store(String jti, String username) {
        redisUtil.setWithExpiration(String.format(REFRESH_TOKEN_BY_USER, username), jti, refreshLife, TimeUnit.MINUTES);
    }

    public boolean validate(String jti, String username) {
        String storedJti = (String) redisUtil.get(String.format(REFRESH_TOKEN_BY_USER, username));
        return jti.equals(storedJti);
    }

    public void deleteIfMatches(String jti, String username) {
        String storedJti = (String) redisUtil.get(String.format(REFRESH_TOKEN_BY_USER, username));
        if (jti.equals(storedJti)) {
            redisUtil.delete(String.format(REFRESH_TOKEN_BY_USER, username));
        }
    }
}
