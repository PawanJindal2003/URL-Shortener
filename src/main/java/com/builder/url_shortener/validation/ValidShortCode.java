package com.builder.url_shortener.validation;

import com.builder.url_shortener.config.Messages;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ValidShortCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidShortCode {
    String message() default "{" + Messages.VALIDATION_SHORT_CODE_INVALID + "}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
