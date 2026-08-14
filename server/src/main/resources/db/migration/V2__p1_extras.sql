CREATE TABLE diary_entries (
    id UUID NOT NULL PRIMARY KEY,
    trip_id UUID NOT NULL REFERENCES trips (id),
    entry_date DATE NOT NULL,
    mood VARCHAR(8) NOT NULL,
    text VARCHAR(4000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX ix_diary_entries_trip_id ON diary_entries (trip_id);

CREATE TABLE wishlist_items (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    name VARCHAR(200) NOT NULL,
    city VARCHAR(120) NOT NULL,
    target_amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    icon_emoji VARCHAR(8),
    note VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX ix_wishlist_items_user_id ON wishlist_items (user_id);

CREATE TABLE media_assets (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    purpose VARCHAR(16) NOT NULL,
    status VARCHAR(24) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    byte_size BIGINT NOT NULL,
    sha256_hex VARCHAR(64),
    upload_token VARCHAR(64) NOT NULL,
    upload_expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    content BYTEA,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uk_media_assets_upload_token ON media_assets (upload_token);
CREATE INDEX ix_media_assets_user_id ON media_assets (user_id);

CREATE TABLE export_jobs (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    trip_id UUID NOT NULL REFERENCES trips (id),
    format VARCHAR(8) NOT NULL,
    status VARCHAR(16) NOT NULL,
    include_tax_free BOOLEAN NOT NULL,
    include_diary BOOLEAN NOT NULL,
    content_type VARCHAR(128),
    content CLOB,
    error_code VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    finished_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX ix_export_jobs_user_id ON export_jobs (user_id);
CREATE INDEX ix_export_jobs_trip_id ON export_jobs (trip_id);
