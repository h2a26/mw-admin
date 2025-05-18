package org.hein.config;

import org.hein.exceptions.handler.SecurityExceptionResolver;
import org.hein.security.token.JwtTokenFilter;
import org.hein.security.token.JwtTokenGenerator;
import org.hein.security.token.JwtTokenParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	private static final String[] PUBLIC_ENDPOINTS = {
			"/api/v1/auth/login",
			"/api/v1/auth/refresh",
			"/v3/api-docs/**",
			"/swagger-ui/**",
			"/swagger-ui.html",
	};

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http,
											JwtTokenFilter jwtTokenFilter,
											SecurityExceptionResolver securityExceptionResolver) throws Exception {
		
		http.csrf(AbstractHttpConfigurer::disable);
		http.cors(cors -> {});
		
		http.authorizeHttpRequests(req -> {
			req.requestMatchers(PUBLIC_ENDPOINTS).permitAll();
			req.anyRequest().authenticated();
		});

		http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http.exceptionHandling(ex -> {
			ex.authenticationEntryPoint(securityExceptionResolver);
			ex.accessDeniedHandler(securityExceptionResolver);
		});

		return http.build();
	}
	
	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
