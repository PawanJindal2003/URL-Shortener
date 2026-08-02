package com.builder.url_shortener.exception;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.builder.url_shortener.config.Messages;
import com.builder.url_shortener.controller.ShortUrlController;
import com.builder.url_shortener.service.ShortUrlService;

@WebMvcTest(ShortUrlController.class)
@Import({GlobalExceptionHandler.class, MessageService.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortUrlService shortUrlService;

    @Test
    void handleValidation_returnsErrorResponseForBlankUrl() throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("url: URL must not be blank"))
                .andExpect(jsonPath("$.path").value("/api/v1/urls"));
    }

    @Test
    void handleApiException_returnsErrorResponseForBadRequest() throws Exception {
        when(shortUrlService.redirect("expired1"))
                .thenThrow(new BadRequestException(Messages.ERROR_URL_BLANK));

        mockMvc.perform(get("/api/v1/urls/expired1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("URL must not be blank"));
    }

    @Test
    void handleApiException_returnsErrorResponseForNotFound() throws Exception {
        when(shortUrlService.getMetadata("missing"))
                .thenThrow(new NotFoundException(Messages.ERROR_SHORT_URL_NOT_FOUND));

        mockMvc.perform(get("/api/v1/urls/missing/metadata"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Short URL not found"));
    }

    @Test
    void handleMalformedJson_returnsErrorResponse() throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));
    }
}
