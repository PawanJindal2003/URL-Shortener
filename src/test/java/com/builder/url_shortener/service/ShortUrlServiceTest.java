package com.builder.url_shortener.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;

import com.builder.url_shortener.config.MessageService;
import com.builder.url_shortener.dto.ShortUrlDto;
import com.builder.url_shortener.entity.ShortUrl;
import com.builder.url_shortener.exception.NotFoundException;
import com.builder.url_shortener.exception.ResourceExpiredException;
import com.builder.url_shortener.repository.ShortUrlRepository;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    private ShortUrlService shortUrlService;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        MessageService messageService = new MessageService(messageSource);
        shortUrlService = new ShortUrlService(shortUrlRepository, messageService);
    }

    @Test
    void createShortUrl_persistsTrimmedUrlWithUniqueShortCode() {
        when(shortUrlRepository.findFirstByOriginalUrl("https://example.com")).thenReturn(Optional.empty());
        when(shortUrlRepository.existsByShortCode(any())).thenReturn(false);
        when(shortUrlRepository.save(any(ShortUrl.class))).thenAnswer(invocation -> {
            ShortUrl entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        ShortUrlDto result = shortUrlService.createShortUrl("  https://example.com  ");

        assertEquals("https://example.com", result.getOriginalUrl());
        assertEquals(0L, result.getClickCount());
        assertEquals(8, result.getShortCode().length());

        ArgumentCaptor<ShortUrl> captor = ArgumentCaptor.forClass(ShortUrl.class);
        verify(shortUrlRepository).save(captor.capture());
        assertEquals("https://example.com", captor.getValue().getOriginalUrl());
    }

    @Test
    void createShortUrl_returnsExistingShortCodeWhenOriginalUrlAlreadyExists() {
        ShortUrl existing = new ShortUrl();
        existing.setId(5L);
        existing.setOriginalUrl("https://example.com");
        existing.setShortCode("existing1");
        existing.setClickCount(10L);

        when(shortUrlRepository.findFirstByOriginalUrl("https://example.com")).thenReturn(Optional.of(existing));

        ShortUrlDto result = shortUrlService.createShortUrl("  https://example.com  ");

        assertEquals("existing1", result.getShortCode());
        assertEquals("https://example.com", result.getOriginalUrl());
        assertEquals(10L, result.getClickCount());
        verify(shortUrlRepository, never()).save(any());
    }

    @Test
    void redirect_returnsOriginalUrlAndIncrementsClickCount() {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode("abc12345");
        shortUrl.setOriginalUrl("https://example.com");
        shortUrl.setClickCount(2L);
        shortUrl.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(shortUrlRepository.findByShortCode("abc12345")).thenReturn(Optional.of(shortUrl));

        String originalUrl = shortUrlService.redirect("abc12345");

        assertEquals("https://example.com", originalUrl);

        ArgumentCaptor<ShortUrl> captor = ArgumentCaptor.forClass(ShortUrl.class);
        verify(shortUrlRepository).save(captor.capture());
        assertEquals(3L, captor.getValue().getClickCount());
    }

    @Test
    void redirect_throwsNotFoundWhenShortCodeDoesNotExist() {
        when(shortUrlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> shortUrlService.redirect("missing"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void redirect_throwsGoneWhenUrlIsExpired() {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode("expired1");
        shortUrl.setOriginalUrl("https://example.com");
        shortUrl.setClickCount(0L);
        shortUrl.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(shortUrlRepository.findByShortCode("expired1")).thenReturn(Optional.of(shortUrl));

        ResourceExpiredException exception = assertThrows(
                ResourceExpiredException.class,
                () -> shortUrlService.redirect("expired1"));

        assertEquals(HttpStatus.GONE, exception.getStatus());
    }

    @Test
    void getMetadata_returnsDtoForExistingShortCode() {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setId(1L);
        shortUrl.setShortCode("abc12345");
        shortUrl.setOriginalUrl("https://example.com");
        shortUrl.setClickCount(5L);
        shortUrl.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(shortUrlRepository.findByShortCode("abc12345")).thenReturn(Optional.of(shortUrl));

        ShortUrlDto result = shortUrlService.getMetadata("abc12345");

        assertEquals(1L, result.getId());
        assertEquals("abc12345", result.getShortCode());
        assertEquals("https://example.com", result.getOriginalUrl());
        assertEquals(5L, result.getClickCount());
        assertEquals(shortUrl.getExpiresAt(), result.getExpiresAt());
    }

    @Test
    void getMetadata_throwsNotFoundWhenShortCodeDoesNotExist() {
        when(shortUrlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> shortUrlService.getMetadata("missing"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void deleteUrl_softDeletesExistingShortCode() {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setId(10L);
        shortUrl.setShortCode("abc12345");

        when(shortUrlRepository.findByShortCode("abc12345")).thenReturn(Optional.of(shortUrl));

        shortUrlService.deleteUrl("abc12345");

        ArgumentCaptor<ShortUrl> captor = ArgumentCaptor.forClass(ShortUrl.class);
        verify(shortUrlRepository).save(captor.capture());
        assertTrue(captor.getValue().isDeleted());
        assertNotNull(captor.getValue().getDeletedAt());
    }

    @Test
    void deleteUrl_throwsNotFoundWhenShortCodeDoesNotExist() {
        when(shortUrlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> shortUrlService.deleteUrl("missing"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
