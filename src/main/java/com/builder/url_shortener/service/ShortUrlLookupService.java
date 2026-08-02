package com.builder.url_shortener.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.builder.url_shortener.config.MessageService;
import com.builder.url_shortener.config.Messages;
import com.builder.url_shortener.dto.CachedShortUrl;
import com.builder.url_shortener.exception.NotFoundException;
import com.builder.url_shortener.repository.ShortUrlRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortUrlLookupService {

    private final ShortUrlRepository shortUrlRepository;
    private final MessageService messageService;

    @Cacheable(value = "shortUrls", key = "#shortCode")
    public CachedShortUrl findByShortCodeOrThrow(String shortCode) {
        log.debug("Fetching short URL from database for code: {}", shortCode);
        return shortUrlRepository.findByShortCode(shortCode)
                .map(CachedShortUrl::fromEntity)
                .orElseThrow(() -> {
                    log.warn(messageService.get(Messages.LOG_SERVICE_SHORT_URL_NOT_FOUND, shortCode));
                    return new NotFoundException(Messages.ERROR_SHORT_URL_NOT_FOUND);
                });
    }

    @CacheEvict(value = "shortUrls", key = "#shortCode")
    public void evict(String shortCode) {
        log.debug("Evicting short URL from cache for code: {}", shortCode);
    }
}
