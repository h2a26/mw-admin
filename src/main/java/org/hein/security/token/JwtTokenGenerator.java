package org.hein.security.token;

import jakarta.annotation.PostConstruct;
import org.hein.commons.enum_.TokenType;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenGenerator {

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

	@Value("${app.token.expiration.access}")
	private int accessLife;

	@Value("${app.token.expiration.refresh}")
	private int refreshLife;

	private SecretKey secretKey;

	@PostConstruct
	public void initBean() {
		this.secretKey = SecretKeys.stringToKey(secretKeyValue);
	}

	public String generateAccessToken(Authentication auth, String accessJti) {
		return generateAccess(auth, accessJti);
	}

	public String generateRefreshToken(Authentication auth, String refreshJti) {
		return generateRefresh(auth, refreshJti);
	}

	private String generateAccess(Authentication auth, String accessJti) {
		var roles = extractRoles(auth);
		var now = Instant.now();
		var expiration = now.plus(accessLife, ChronoUnit.MINUTES);

		return Jwts.builder()
				.subject(auth.getName())
				.issuer(issuer)
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiration))
				.claim(roleKey, roles)
				.claim(typeKey, TokenType.Access.name())
				.claim(jtiKey, accessJti)
				.signWith(secretKey)
				.compact();
	}

	private String generateRefresh(Authentication auth, String refreshJti) {
		var roles = extractRoles(auth);
		var now = Instant.now();
		var expiration = now.plus(refreshLife, ChronoUnit.MINUTES);

		return Jwts.builder()
				.subject(auth.getName())
				.issuer(issuer)
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiration))
				.claim(roleKey, roles)
				.claim(typeKey, TokenType.Refresh.name())
				.claim(jtiKey, refreshJti)
				.signWith(secretKey)
				.compact();
	}

	private String extractRoles(Authentication auth) {
		return auth.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));
	}
}