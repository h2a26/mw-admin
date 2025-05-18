package org.hein.security.token;

import lombok.RequiredArgsConstructor;
import org.hein.utils.RedisUtil;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {
    private final RedisUtil redisUtil;

    private static final String PREFIX = "auth:refresh:";

    public void store(String token, String username, long durationMinutes) {
        redisUtil.setWithExpiration(PREFIX + token, username, durationMinutes, TimeUnit.MINUTES);
    }

    public boolean validate(String token, String username) {
        Object stored = redisUtil.get(PREFIX + token);
        return username.equals(stored);
    }

    public void delete(String token) {
        redisUtil.delete(PREFIX + token);
    }
}
