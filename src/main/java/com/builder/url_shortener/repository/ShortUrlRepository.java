package com.builder.url_shortener.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.builder.url_shortener.entity.ShortUrl;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    Optional<ShortUrl> findFirstByOriginalUrl(String originalUrl);

    boolean existsByShortCode(String shortCode);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ShortUrl s SET s.clickCount = s.clickCount + 1 WHERE s.shortCode = :shortCode AND s.deleted = false")
    int incrementClickCount(@Param("shortCode") String shortCode);
}
