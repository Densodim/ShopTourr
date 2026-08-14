CREATE TABLE users (
    id UUID NOT NULL PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(120) NOT NULL,
    display_name VARCHAR(80) NOT NULL,
    locale VARCHAR(5) NOT NULL,
    preferred_currency VARCHAR(3) NOT NULL,
    theme VARCHAR(16) NOT NULL,
    push_notifications_enabled BOOLEAN NOT NULL,
    dark_mode BOOLEAN NOT NULL,
    avatar_url VARCHAR(512),
    premium_plan VARCHAR(16) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uk_users_email ON users (email);

CREATE TABLE refresh_sessions (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    token_hash VARCHAR(64) NOT NULL,
    device_name VARCHAR(120),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uk_refresh_sessions_token_hash ON refresh_sessions (token_hash);
CREATE INDEX ix_refresh_sessions_user_id ON refresh_sessions (user_id);

CREATE TABLE idempotency_keys (
    user_id UUID NOT NULL,
    idempotency_key VARCHAR(80) NOT NULL,
    route VARCHAR(200) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    status_code INTEGER NOT NULL,
    response_body CLOB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (user_id, idempotency_key)
);

CREATE TABLE trips (
    id UUID NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    city VARCHAR(120) NOT NULL,
    country VARCHAR(120) NOT NULL,
    country_code VARCHAR(2),
    flag_emoji VARCHAR(16),
    status VARCHAR(16) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    budget_amount NUMERIC(19, 2) NOT NULL,
    budget_currency VARCHAR(3) NOT NULL,
    default_vat_rate NUMERIC(6, 3) NOT NULL,
    fx_trip_currency VARCHAR(3),
    fx_quote_currency VARCHAR(3),
    fx_rate NUMERIC(19, 8),
    fx_rate_date DATE,
    fx_provider VARCHAR(40),
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX ix_trips_user_id ON trips (user_id);

CREATE TABLE travelers (
    id UUID NOT NULL PRIMARY KEY,
    trip_id UUID NOT NULL REFERENCES trips (id),
    name VARCHAR(60) NOT NULL,
    color_hex VARCHAR(7) NOT NULL,
    avatar_glyph VARCHAR(2) NOT NULL,
    is_owner BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX ix_travelers_trip_id ON travelers (trip_id);

CREATE TABLE purchases (
    id UUID NOT NULL PRIMARY KEY,
    trip_id UUID NOT NULL REFERENCES trips (id),
    name VARCHAR(200) NOT NULL,
    category VARCHAR(24) NOT NULL,
    gross_amount NUMERIC(19, 2) NOT NULL,
    net_amount NUMERIC(19, 2) NOT NULL,
    vat_amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    vat_rate NUMERIC(6, 3) NOT NULL,
    vat_included BOOLEAN NOT NULL,
    tax_refund_eligible BOOLEAN NOT NULL,
    place VARCHAR(200),
    purchase_date DATE NOT NULL,
    purchase_time TIME,
    receipt_media_id UUID,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX ix_purchases_trip_id ON purchases (trip_id);

CREATE TABLE purchase_splits (
    id UUID NOT NULL PRIMARY KEY,
    purchase_id UUID NOT NULL REFERENCES purchases (id),
    traveler_id UUID NOT NULL REFERENCES travelers (id),
    share_amount NUMERIC(19, 2) NOT NULL
);

CREATE INDEX ix_purchase_splits_purchase_id ON purchase_splits (purchase_id);
