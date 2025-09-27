package pl.coderslab.provider.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CurrencyRateDto(
        Long id,
        String currency,
        BigDecimal rate,
        OffsetDateTime timestamp
) {
}