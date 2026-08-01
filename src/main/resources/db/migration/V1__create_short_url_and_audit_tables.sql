CREATE TABLE short_url (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_url VARCHAR(2048) NOT NULL,
    short_code VARCHAR(32) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NULL,
    click_count BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_short_url_short_code UNIQUE (short_code)
);

CREATE INDEX idx_short_url_expires_at ON short_url (expires_at);
