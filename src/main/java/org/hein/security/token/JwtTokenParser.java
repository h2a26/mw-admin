package org.hein.security.token;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.hein.commons.enum_.TokenType;
import org.hein.exceptions.ApiJwtTokenExpirationException;
import org.hein.exceptions.ApiJwtTokenInvalidationException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.hein.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtTokenParser {

	@Value("${app.token.secret}")
	private String secretKeyValue;

	@Value("${app.token.issuer}")
	private String issuer;

	@Value("${app.token.role.key}")
	private String roleKey;

	@Value("${app.token.type.key}")
	private String typeKey;

	@Value("${app.token.jti.key}")
	private String jtiKey;

	private SecretKey secretKey;

	private final JtiTokenStore jtiTokenStore;

	@PostConstruct
	public void initBean() {
		this.secretKey = SecretKeys.stringToKey(secretKeyValue);
	}

	public Authentication parse(TokenType expectedType, String jwtToken) {
		try {
			String token = TokenUtils.extractToken(jwtToken);

			var jwt = Jwts.parser()
					.requireIssuer(issuer)
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token);

			var claims = jwt.getPayload();

			var typeValue = claims.get(typeKey, String.class);
			if (!expectedType.name().equals(typeValue)) {
				throw new ApiJwtTokenInvalidationException("Invalid Token type");
			}

			var username = claims.getSubject();

			var jtiValue = claims.get(jtiKey, String.class);

			if (expectedType.name().equals(TokenType.Access.name()) && !jtiTokenStore.validateAccessJti(jtiValue, username)) {
				throw new ApiJwtTokenInvalidationException("Expired access token.");
			}

			if (expectedType.name().equals(TokenType.Refresh.name()) && !jtiTokenStore.validateRefreshJti(jtiValue, username)) {
				throw new ApiJwtTokenInvalidationException("Expired refresh token.");
			}

			var roles = Arrays.stream(claims.get(roleKey, String.class).split(","))
					.map(SimpleGrantedAuthority::new).toList();

			return UsernamePasswordAuthenticationToken.authenticated(username, null, roles);

		} catch (ExpiredJwtException e) {
			if (expectedType == TokenType.Access) {
				throw new ApiJwtTokenExpirationException("Expired access token.");
			} else {
				throw new ApiJwtTokenInvalidationException("Expired refresh token.");
			}
		} catch (JwtException e) {
			throw new ApiJwtTokenInvalidationException("Token is invalid.", e);
		}
	}

	public String extractJti(String token) {
		token = TokenUtils.extractToken(token);
		var claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return claims.get(jtiKey, String.class);
	}

}
