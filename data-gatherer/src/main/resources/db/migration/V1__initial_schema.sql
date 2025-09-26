CREATE TABLE IF NOT EXISTS exchange_rate_snapshot (
                                                      id              BIGSERIAL PRIMARY KEY,
                                                      currency_code   VARCHAR(3)    NOT NULL,
    rate            NUMERIC(19,6) NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
                                  );

CREATE UNIQUE INDEX IF NOT EXISTS exchange_rate_snapshot_currency_code_idx
    ON exchange_rate_snapshot (currency_code);