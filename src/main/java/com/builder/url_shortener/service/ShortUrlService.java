package com.builder.url_shortener.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.builder.url_shortener.dto.ShortUrlDto;
import com.builder.url_shortener.entity.ShortUrl;
import com.builder.url_shortener.mapper.ShortUrlMapper;
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
    private final ShortUrlMapper shortUrlMapper;

    @Transactional
    public ShortUrlDto createShortUrl(String originalUrl) {
        String normalizedUrl = originalUrl == null ? "" : originalUrl.trim();
        if (normalizedUrl.isBlank()) {
            log.warn("Rejected short URL creation: blank URL");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL must not be blank");
        }

        return shortUrlRepository.findFirstByOriginalUrl(normalizedUrl)
                .map(existing -> {
                    log.debug("Returning existing short URL for code={}", existing.getShortCode());
                    return shortUrlMapper.toDto(existing);
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
        log.info("Created short URL code={} expiresAt={}", saved.getShortCode(), saved.getExpiresAt());
        return shortUrlMapper.toDto(saved);
    }

    @Transactional
    public String redirect(String shortCode) {
        ShortUrl shortUrl = findByShortCodeOrThrow(shortCode);

        if (shortUrl.getExpiresAt() != null
                && shortUrl.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Redirect rejected: shortCode={} has expired", shortCode);
            throw new ResponseStatusException(
                    HttpStatus.GONE, "Short URL has expired");
        }

        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        shortUrlRepository.save(shortUrl);
        log.debug("Redirecting shortCode={} clickCount={}", shortCode, shortUrl.getClickCount());

        return shortUrl.getOriginalUrl();
    }

    public ShortUrlDto getMetadata(String shortCode) {
        ShortUrl shortUrl = findByShortCodeOrThrow(shortCode);
        return shortUrlMapper.toDto(shortUrl);
    }

    @Transactional
    public void deleteUrl(String shortCode) {
        ShortUrl shortUrl = findByShortCodeOrThrow(shortCode);
        shortUrlRepository.delete(shortUrl);
        log.info("Deleted short URL code={}", shortCode);
    }

    private ShortUrl findByShortCodeOrThrow(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.warn("Short URL not found for code={}", shortCode);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Short URL not found");
                });
    }

    private String generateUniqueShortCode() {
        for (int attempt = 0; attempt < MAX_SHORT_CODE_GENERATION_ATTEMPTS; attempt++) {
            String shortCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            if (!shortUrlRepository.existsByShortCode(shortCode)) {
                return shortCode;
            }
        }
        log.error("Failed to generate unique short code after {} attempts", MAX_SHORT_CODE_GENERATION_ATTEMPTS);
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "Unable to generate unique short code");
    }
}
