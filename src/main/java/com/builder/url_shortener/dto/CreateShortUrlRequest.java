package com.builder.url_shortener.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateShortUrlRequest {
    @NotBlank(message = "URL must not be blank")
    private String url;
}
