package pl.coderslab.currencyalert.common.messaging;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Message published whenever an exchange rate change exceeds a configured threshold.
 */
public record CurrencyRateChangeMessage(
        @JsonProperty("baseCurrency") String baseCurrency,
        @JsonProperty("currency") String currency,
        @JsonProperty("newRate") BigDecimal newRate,
        @JsonProperty("changePercentage") BigDecimal changePercentage,
        @JsonProperty("timestamp") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp
) implements Serializable {
}
