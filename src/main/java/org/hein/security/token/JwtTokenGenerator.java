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
import java.util.UUID;
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

	@Value("${app.token.expiration.refresh}")
	private int refreshLife;

	@Value("${app.token.expiration.access}")
	private int accessLife;

	private SecretKey secretKey;

	@PostConstruct
	public void initBean() {
		this.secretKey = SecretKeys.stringToKey(secretKeyValue);
	}

	public String generate(TokenType type, Authentication auth, String jti) {
		var roles = auth.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));

		Instant issuedAt = Instant.now();
		Instant expiration = (type == TokenType.Access)
				? issuedAt.plus(accessLife, ChronoUnit.MINUTES)
				: issuedAt.plus(refreshLife, ChronoUnit.MINUTES);

		return Jwts.builder()
				.subject(auth.getName())
				.issuer(issuer)
				.issuedAt(Date.from(issuedAt))
				.expiration(Date.from(expiration))
				.claim(roleKey, roles)
				.claim(typeKey, type.name())
				.claim(jtiKey, jti)
				.signWith(secretKey)
				.compact();
	}

	public String generate(TokenType type, Authentication auth) {
		return generate(type, auth, UUID.randomUUID().toString());
	}
}