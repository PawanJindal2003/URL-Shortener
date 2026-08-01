package com.builder.url_shortener.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShortUrlDto {
    private Long id;
    private String originalUrl;
    private String shortCode;
    private LocalDateTime expiresAt;
    private Long clickCount;
}
