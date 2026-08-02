package com.builder.url_shortener.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import com.builder.url_shortener.config.Messages;

@Data
public class CreateShortUrlRequest {
    @NotBlank(message = "{" + Messages.VALIDATION_URL_NOT_BLANK + "}")
    private String url;
}
