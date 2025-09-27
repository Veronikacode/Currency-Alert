package pl.coderslab.provider.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubscriptionDto(
        Long id,
        @NotBlank(message = "Base currency is required")
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "Base currency must be a 3-letter code")
        String baseCurrency,
        @NotBlank(message = "Target currency is required")
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "Target currency must be a 3-letter code")
        String targetCurrency,
        @NotNull(message = "Threshold percent is required")
        @DecimalMin(value = "0.000", inclusive = false, message = "Threshold must be positive")
        BigDecimal thresholdPercent,
        Boolean active,
        OffsetDateTime createdAt
) {
}