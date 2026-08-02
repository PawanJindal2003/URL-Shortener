package com.builder.url_shortener.validation;

import com.builder.url_shortener.config.Messages;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidUrlValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUrl {
    String message() default "{" + Messages.VALIDATION_URL_INVALID + "}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}