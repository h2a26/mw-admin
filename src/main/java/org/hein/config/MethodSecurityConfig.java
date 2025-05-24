package org.hein.config;

import org.hein.security.RbacPermissionEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true
)
public class MethodSecurityConfig {
    private final PermissionEvaluator rbacPermissionEvaluator;

    public MethodSecurityConfig(RbacPermissionEvaluator rbacPermissionEvaluator) {
        this.rbacPermissionEvaluator = rbacPermissionEvaluator;
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(rbacPermissionEvaluator);
        return expressionHandler;
    }
}
