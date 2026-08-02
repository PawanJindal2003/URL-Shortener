package com.builder.url_shortener.validation;

import com.builder.url_shortener.config.Messages;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidShortCodeValidator implements ConstraintValidator<ValidShortCode, String> {

    private static final int MAX_LENGTH = 32;
    private static final String ALPHANUMERIC_PATTERN = "[a-zA-Z0-9]+";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "{" + Messages.VALIDATION_SHORT_CODE_NOT_BLANK + "}")
                    .addConstraintViolation();
            return false;
        }
        return value.length() <= MAX_LENGTH && value.matches(ALPHANUMERIC_PATTERN);
    }
}
