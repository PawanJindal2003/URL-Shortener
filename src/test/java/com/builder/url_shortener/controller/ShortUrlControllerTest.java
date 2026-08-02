package com.builder.url_shortener.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.builder.url_shortener.config.MessageService;
import com.builder.url_shortener.dto.ShortUrlDto;
import com.builder.url_shortener.service.ShortUrlService;

@WebMvcTest(ShortUrlController.class)
@Import(MessageService.class)
class ShortUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortUrlService shortUrlService;

    @Test
    void createShortUrl_acceptsUrlInRequestBody() throws Exception {
        ShortUrlDto created = new ShortUrlDto();
        created.setOriginalUrl("https://example.com/path");
        created.setShortCode("abc12345");

        when(shortUrlService.createShortUrl("https://example.com/path")).thenReturn(created);

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/path\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com/path"))
                .andExpect(jsonPath("$.shortCode").value("abc12345"));

        verify(shortUrlService).createShortUrl(eq("https://example.com/path"));
    }

    @Test
    void getMetadata_returnsMetadataForShortCode() throws Exception {
        ShortUrlDto metadata = new ShortUrlDto();
        metadata.setShortCode("abc12345");
        metadata.setOriginalUrl("https://example.com");
        metadata.setClickCount(5L);

        when(shortUrlService.getMetadata("abc12345")).thenReturn(metadata);

        mockMvc.perform(get("/api/v1/urls/abc12345/metadata"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("abc12345"))
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
                .andExpect(jsonPath("$.clickCount").value(5));

        verify(shortUrlService).getMetadata(eq("abc12345"));
    }

    @Test
    void redirectByShortCode_returnsFoundWithLocationHeader() throws Exception {
        when(shortUrlService.redirect("abc12345")).thenReturn("https://example.com");

        mockMvc.perform(get("/api/v1/urls/abc12345"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com"));

        verify(shortUrlService).redirect(eq("abc12345"));
    }
}
