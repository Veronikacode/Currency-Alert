ALTER TABLE currency_rates
    ADD COLUMN IF NOT EXISTS base_currency VARCHAR(3);

UPDATE currency_rates
SET base_currency = 'USD'
WHERE base_currency IS NULL;

ALTER TABLE currency_rates
    ALTER COLUMN base_currency SET NOT NULL;

DROP INDEX IF EXISTS currency_rates_code_timestamp_idx;

CREATE UNIQUE INDEX IF NOT EXISTS currency_rates_base_code_timestamp_idx
    ON currency_rates (base_currency, currency_code, timestamp);