package com.builder.url_shortener.config;

public final class Messages {

    private Messages() {
    }

    // Validation
    public static final String VALIDATION_URL_NOT_BLANK = "validation.url.notBlank";
    public static final String VALIDATION_URL_INVALID = "validation.url.invalid";
    public static final String VALIDATION_SHORT_CODE_NOT_BLANK = "validation.shortCode.notBlank";
    public static final String VALIDATION_SHORT_CODE_INVALID = "validation.shortCode.invalid";

    // API error messages
    public static final String ERROR_URL_BLANK = "error.url.blank";
    public static final String ERROR_SHORT_URL_NOT_FOUND = "error.shortUrl.notFound";
    public static final String ERROR_RESOURCE_NOT_FOUND = "error.resource.notFound";
    public static final String ERROR_SHORT_URL_EXPIRED = "error.shortUrl.expired";
    public static final String ERROR_SHORT_URL_CODE_GENERATION_FAILED = "error.shortUrl.codeGenerationFailed";
    public static final String ERROR_MALFORMED_JSON = "error.malformedJson";
    public static final String ERROR_UNEXPECTED = "error.unexpected";

    // API error titles
    public static final String ERROR_TITLE_BAD_REQUEST = "error.title.badRequest";
    public static final String ERROR_TITLE_VALIDATION_FAILED = "error.title.validationFailed";
    public static final String ERROR_TITLE_NOT_FOUND = "error.title.notFound";
    public static final String ERROR_TITLE_GONE = "error.title.gone";
    public static final String ERROR_TITLE_INTERNAL_SERVER_ERROR = "error.title.internalServerError";

    // Controller logs
    public static final String LOG_CONTROLLER_SHORT_URL_CREATE_REQUEST = "log.controller.shortUrl.create.request";
    public static final String LOG_CONTROLLER_SHORT_URL_CREATE_SUCCESS = "log.controller.shortUrl.create.success";
    public static final String LOG_CONTROLLER_SHORT_URL_REDIRECT_REQUEST = "log.controller.shortUrl.redirect.request";
    public static final String LOG_CONTROLLER_SHORT_URL_METADATA_REQUEST = "log.controller.shortUrl.metadata.request";
    public static final String LOG_CONTROLLER_SHORT_URL_DELETE_REQUEST = "log.controller.shortUrl.delete.request";

    // Service logs
    public static final String LOG_SERVICE_SHORT_URL_CREATE_REJECTED_BLANK = "log.service.shortUrl.create.rejectedBlank";
    public static final String LOG_SERVICE_SHORT_URL_CREATE_EXISTING = "log.service.shortUrl.create.existing";
    public static final String LOG_SERVICE_SHORT_URL_CREATE_SAVED = "log.service.shortUrl.create.saved";
    public static final String LOG_SERVICE_SHORT_URL_REDIRECT_EXPIRED = "log.service.shortUrl.redirect.expired";
    public static final String LOG_SERVICE_SHORT_URL_REDIRECT_SUCCESS = "log.service.shortUrl.redirect.success";
    public static final String LOG_SERVICE_SHORT_URL_DELETE_SUCCESS = "log.service.shortUrl.delete.success";
    public static final String LOG_SERVICE_SHORT_URL_NOT_FOUND = "log.service.shortUrl.notFound";
    public static final String LOG_SERVICE_SHORT_URL_CODE_GENERATION_FAILED = "log.service.shortUrl.codeGenerationFailed";

    // Exception handler logs
    public static final String LOG_EXCEPTION_UNEXPECTED = "log.exception.unexpected";
}
