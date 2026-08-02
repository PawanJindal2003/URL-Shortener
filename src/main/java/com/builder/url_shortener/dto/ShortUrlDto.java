package com.builder.url_shortener.dto;

import com.builder.url_shortener.entity.ShortUrl;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShortUrlDto {
    private Long id;
    private String originalUrl;
    private String shortCode;
    private LocalDateTime expiresAt;
    private Long clickCount;

    public static ShortUrlDto toDto(ShortUrl entity) {
        if (entity == null) {
            return null;
        }
        ShortUrlDto dto = new ShortUrlDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public ShortUrl toEntity() {
        ShortUrl entity = new ShortUrl();
        BeanUtils.copyProperties(this, entity);
        if (entity.getClickCount() == null) {
            entity.setClickCount(0L);
        }
        return entity;
    }
}
