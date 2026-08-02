package com.builder.url_shortener.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.builder.url_shortener.config.MessageService;
import com.builder.url_shortener.config.Messages;
import com.builder.url_shortener.dto.CachedShortUrl;
import com.builder.url_shortener.dto.ShortUrlDto;
import com.builder.url_shortener.entity.ShortUrl;
import com.builder.url_shortener.exception.InternalServerException;
import com.builder.url_shortener.exception.NotFoundException;
import com.builder.url_shortener.exception.ResourceExpiredException;
import com.builder.url_shortener.repository.ShortUrlRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("java:S8688")
public class ShortUrlService {
    private static final int DEFAULT_EXPIRATION_DAYS = 2;
    private static final int MAX_SHORT_CODE_GENERATION_ATTEMPTS = 5;

    private final ShortUrlRepository shortUrlRepository;
    private final ShortUrlLookupService shortUrlLookupService;
    private final MessageService messageService;

    @Transactional
    public ShortUrlDto createShortUrl(String originalUrl) {
        String normalizedUrl = originalUrl.trim();

        return shortUrlRepository.findFirstByOriginalUrl(normalizedUrl)
                .map(existing -> {
                    log.debug(messageService.get(Messages.LOG_SERVICE_SHORT_URL_CREATE_EXISTING, existing.getShortCode()));
                    return ShortUrlDto.toDto(existing);
                })
                .orElseGet(() -> createNewShortUrl(normalizedUrl));
    }

    private ShortUrlDto createNewShortUrl(String normalizedUrl) {
        ShortUrl entity = new ShortUrl();
        entity.setOriginalUrl(normalizedUrl);
        entity.setShortCode(generateUniqueShortCode());
        entity.setClickCount(0L);
        entity.setExpiresAt(LocalDateTime.now().plusDays(DEFAULT_EXPIRATION_DAYS));

        ShortUrl saved = shortUrlRepository.save(entity);
        log.info(messageService.get(
                Messages.LOG_SERVICE_SHORT_URL_CREATE_SAVED,
                saved.getShortCode(),
                saved.getExpiresAt()));
        return ShortUrlDto.toDto(saved);
    }

    @Transactional
    public String redirect(String shortCode) {
        CachedShortUrl cached = shortUrlLookupService.findByShortCodeOrThrow(shortCode);

        if (cached.expiresAt() != null && cached.expiresAt().isBefore(LocalDateTime.now())) {
            log.warn(messageService.get(Messages.LOG_SERVICE_SHORT_URL_REDIRECT_EXPIRED, shortCode));
            throw new ResourceExpiredException(Messages.ERROR_SHORT_URL_EXPIRED);
        }

        shortUrlRepository.incrementClickCount(shortCode);
        log.debug(messageService.get(Messages.LOG_SERVICE_SHORT_URL_REDIRECT_SUCCESS, shortCode, "incremented"));

        return cached.originalUrl();
    }

    public ShortUrlDto getMetadata(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.warn(messageService.get(Messages.LOG_SERVICE_SHORT_URL_NOT_FOUND, shortCode));
                    return new NotFoundException(Messages.ERROR_SHORT_URL_NOT_FOUND);
                });
        return ShortUrlDto.toDto(shortUrl);
    }

    @Transactional
    public void deleteUrl(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new NotFoundException(Messages.ERROR_SHORT_URL_NOT_FOUND));
        shortUrl.setDeleted(true);
        shortUrl.setDeletedAt(LocalDateTime.now());
        shortUrlRepository.save(shortUrl);
        shortUrlLookupService.evict(shortCode);
        log.info(messageService.get(Messages.LOG_SERVICE_SHORT_URL_DELETE_SUCCESS, shortCode));
    }

    private String generateUniqueShortCode() {
        for (int attempt = 0; attempt < MAX_SHORT_CODE_GENERATION_ATTEMPTS; attempt++) {
            String shortCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            if (!shortUrlRepository.existsByShortCode(shortCode)) {
                return shortCode;
            }
        }
        log.error(messageService.get(
                Messages.LOG_SERVICE_SHORT_URL_CODE_GENERATION_FAILED,
                MAX_SHORT_CODE_GENERATION_ATTEMPTS));
        throw new InternalServerException(Messages.ERROR_SHORT_URL_CODE_GENERATION_FAILED);
    }
}
