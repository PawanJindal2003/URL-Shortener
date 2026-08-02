package com.builder.url_shortener.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.builder.url_shortener.dto.CreateShortUrlRequest;
import com.builder.url_shortener.dto.ShortUrlDto;
import com.builder.url_shortener.service.ShortUrlService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ShortUrlController {
    private final ShortUrlService shortUrlService;

    @PostMapping("v1/urls")
    public ResponseEntity<ShortUrlDto> createShortUrl(@Valid @RequestBody CreateShortUrlRequest request) {
        log.info("Creating short URL for request");
        ShortUrlDto createdShortUrl = shortUrlService.createShortUrl(request.getUrl());
        log.info("Short URL created with code={}", createdShortUrl.getShortCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdShortUrl);
    }

    @GetMapping("v1/urls/{shortCode}")
    public ResponseEntity<Void> redirectByShortCode(@PathVariable String shortCode) {
        log.info("Redirect requested for shortCode={}", shortCode);
        String originalUrl = shortUrlService.redirect(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    @GetMapping("v1/urls/{shortCode}/metadata")
    public ResponseEntity<ShortUrlDto> getMetadata(@PathVariable String shortCode) {
        log.debug("Metadata requested for shortCode={}", shortCode);
        ShortUrlDto metadata = shortUrlService.getMetadata(shortCode);
        return ResponseEntity.ok(metadata);
    }

    @DeleteMapping("v1/urls/{shortCode}")
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortCode) {
        log.info("Delete requested for shortCode={}", shortCode);
        shortUrlService.deleteUrl(shortCode);
        return ResponseEntity.noContent().build();
    }
}
