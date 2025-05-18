package org.hein.security.token;

import lombok.RequiredArgsConstructor;
import org.hein.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    @Value("${app.token.expiration.refresh}")
    private int refreshLife;

    private final RedisUtil redisUtil;

    private static final String KEY_USER = "auth:refresh:user:%s"; // username → jti

    public void store(String jti, String username) {
        redisUtil.setWithExpiration(String.format(KEY_USER, username), jti, refreshLife, TimeUnit.MINUTES);
    }

    public boolean validate(String jti, String username) {
        String storedJti = (String) redisUtil.get(String.format(KEY_USER, username));
        return jti.equals(storedJti);
    }

    public void deleteIfMatches(String jti, String username) {
        String storedJti = (String) redisUtil.get(String.format(KEY_USER, username));
        if (jti.equals(storedJti)) {
            redisUtil.delete(String.format(KEY_USER, username));
        }
    }
}
