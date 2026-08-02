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

import com.builder.url_shortener.config.MessageService;
import com.builder.url_shortener.config.Messages;
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
    private final MessageService messageService;

    @PostMapping("v1/urls")
    public ResponseEntity<ShortUrlDto> createShortUrl(@Valid @RequestBody CreateShortUrlRequest request) {
        log.info(messageService.get(Messages.LOG_CONTROLLER_SHORT_URL_CREATE_REQUEST));
        ShortUrlDto createdShortUrl = shortUrlService.createShortUrl(request.getUrl());
        log.info(messageService.get(Messages.LOG_CONTROLLER_SHORT_URL_CREATE_SUCCESS, createdShortUrl.getShortCode()));
        return ResponseEntity.status(HttpStatus.CREATED).body(createdShortUrl);
    }

    @GetMapping("v1/urls/{shortCode}")
    public ResponseEntity<Void> redirectByShortCode(@PathVariable String shortCode) {
        log.info(messageService.get(Messages.LOG_CONTROLLER_SHORT_URL_REDIRECT_REQUEST, shortCode));
        String originalUrl = shortUrlService.redirect(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    @GetMapping("v1/urls/{shortCode}/metadata")
    public ResponseEntity<ShortUrlDto> getMetadata(@PathVariable String shortCode) {
        log.debug(messageService.get(Messages.LOG_CONTROLLER_SHORT_URL_METADATA_REQUEST, shortCode));
        ShortUrlDto metadata = shortUrlService.getMetadata(shortCode);
        return ResponseEntity.ok(metadata);
    }

    @DeleteMapping("v1/urls/{shortCode}")
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortCode) {
        log.info(messageService.get(Messages.LOG_CONTROLLER_SHORT_URL_DELETE_REQUEST, shortCode));
        shortUrlService.deleteUrl(shortCode);
        return ResponseEntity.noContent().build();
    }
}
