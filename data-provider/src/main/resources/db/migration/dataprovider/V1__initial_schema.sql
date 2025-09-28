CREATE TABLE IF NOT EXISTS users (
    id             BIGSERIAL PRIMARY KEY,
    email          VARCHAR(255)        NOT NULL,
    password_hash  VARCHAR(255)        NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (email)
    );

CREATE TABLE IF NOT EXISTS subscriptions (
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT             NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    base_currency      VARCHAR(3)         NOT NULL,
    target_currency    VARCHAR(3)         NOT NULL,
    threshold_percent  NUMERIC(6,3)       NOT NULL,
    active             BOOLEAN            NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
                                                                                                                      );

CREATE INDEX IF NOT EXISTS subscriptions_user_idx ON subscriptions (user_id);
CREATE INDEX IF NOT EXISTS subscriptions_currency_idx ON subscriptions (target_currency);

CREATE TABLE IF NOT EXISTS currency_rates (
    id             BIGSERIAL PRIMARY KEY,
    currency_code  VARCHAR(3)         NOT NULL,
    rate           NUMERIC(19,6)      NOT NULL,
    timestamp      TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 );

CREATE UNIQUE INDEX IF NOT EXISTS currency_rates_code_timestamp_idx
    ON currency_rates (currency_code, timestamp);

CREATE TABLE IF NOT EXISTS notifications (
    id                BIGSERIAL PRIMARY KEY,
    subscription_id   BIGINT             NOT NULL REFERENCES subscriptions (id) ON DELETE CASCADE,
    currency_rate_id  BIGINT             NOT NULL REFERENCES currency_rates (id) ON DELETE CASCADE,
    status            VARCHAR(32)        NOT NULL DEFAULT 'PENDING',
    message           TEXT,
    sent_at           TIMESTAMP WITH TIME ZONE,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
                                                                                                                             );

CREATE INDEX IF NOT EXISTS notifications_subscription_idx ON notifications (subscription_id);
CREATE INDEX IF NOT EXISTS notifications_currency_rate_idx ON notifications (currency_rate_id);