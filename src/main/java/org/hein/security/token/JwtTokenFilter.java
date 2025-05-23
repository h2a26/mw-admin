package org.hein.security.token;

import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hein.commons.enum_.TokenType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hein.exceptions.ApiJwtTokenInvalidationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter{

	private final JwtTokenParser jwtTokenParser;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,@NonNull FilterChain filterChain)
			throws ServletException, IOException {

		var jwtToken = request.getHeader("Authorization");

		try{
			if(StringUtils.hasLength(jwtToken) && jwtToken.startsWith("Bearer ")) {
				log.info("jwtToken: {}", jwtToken);
				var authentication = jwtTokenParser.parse(TokenType.Access, jwtToken);

				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}catch (JwtException ex) {
			throw new ApiJwtTokenInvalidationException("Token is invalid." + ex.getMessage());
		}

		filterChain.doFilter(request, response);
	}

	
}
