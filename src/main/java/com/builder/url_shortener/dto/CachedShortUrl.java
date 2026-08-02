package com.builder.url_shortener.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.builder.url_shortener.entity.ShortUrl;

public record CachedShortUrl(
        Long id,
        String shortCode,
        String originalUrl,
        LocalDateTime expiresAt) implements Serializable {

    public static CachedShortUrl fromEntity(ShortUrl entity) {
        return new CachedShortUrl(
                entity.getId(),
                entity.getShortCode(),
                entity.getOriginalUrl(),
                entity.getExpiresAt());
    }
}
