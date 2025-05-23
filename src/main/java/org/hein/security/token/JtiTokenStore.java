package org.hein.security.token;

import lombok.RequiredArgsConstructor;
import org.hein.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static org.hein.commons.constant.RedisKeys.*;

@Component
@RequiredArgsConstructor
public class JtiTokenStore {

    @Value("${app.token.expiration.access}")
    private int accessLife;

    @Value("${app.token.expiration.refresh}")
    private int refreshLife;

    private final RedisUtil redisUtil;


    public void storeAccessJti(String jti, String username) {
        redisUtil.setWithExpiration(String.format(ACCESS_TOKEN_BY_USER, username), jti, accessLife, TimeUnit.MINUTES);
    }

    public void storeRefreshJti(String jti, String username) {
        redisUtil.setWithExpiration(String.format(REFRESH_TOKEN_BY_USER, username), jti, refreshLife, TimeUnit.MINUTES);
    }

    public boolean validateAccessJti(String jti, String username) {
        String stored = (String) redisUtil.get(String.format(ACCESS_TOKEN_BY_USER, username));
        return jti.equals(stored);
    }

    public boolean validateRefreshJti(String jti, String username) {
        String stored = (String) redisUtil.get(String.format(REFRESH_TOKEN_BY_USER, username));
        return jti.equals(stored);
    }

    public void revokeTokens(String username) {
        String accessKey = String.format(ACCESS_TOKEN_BY_USER, username);
        String refreshKey = String.format(REFRESH_TOKEN_BY_USER, username);
        redisUtil.delete(accessKey);
        redisUtil.delete(refreshKey);
    }
}
