package pl.coderslab.gatherer.exchangerate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public record ExchangeRateSnapshot(String baseCurrency,
                                   Map<String, BigDecimal> rates,
                                   Instant retrievedAt) {

    public ExchangeRateSnapshot {
        rates = Collections.unmodifiableMap(Objects.requireNonNull(rates, "rates"));
    }
}
