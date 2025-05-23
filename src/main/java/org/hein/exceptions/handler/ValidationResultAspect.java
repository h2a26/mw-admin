package org.hein.exceptions.handler;

import org.hein.exceptions.ApiValidationException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;

@Aspect
@Configuration
public class ValidationResultAspect {
	
	@Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
	public void apiMethod() {}

    @Before(value = "apiMethod() && args(.., result)", argNames = "result")
	public void handle(BindingResult result) {

		if(result.hasErrors()) {
			throw new ApiValidationException(result.getFieldErrors()
					.stream()
					.map(DefaultMessageSourceResolvable::getDefaultMessage).toList());
		}
	}
}
