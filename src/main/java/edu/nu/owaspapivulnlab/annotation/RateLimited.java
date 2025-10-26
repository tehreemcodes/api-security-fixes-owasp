package edu.nu.owaspapivulnlab.annotation;

import edu.nu.owaspapivulnlab.service.RateLimitService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    RateLimitService.RateLimitType value() default RateLimitService.RateLimitType.GENERAL;
}
