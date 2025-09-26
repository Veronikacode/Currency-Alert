package pl.coderslab.currencyalert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.coderslab.currencyalert.common.messaging.CurrencyRateChangeMessage;
import pl.coderslab.gatherer.config.ExchangeRateProperties;
import pl.coderslab.gatherer.exchangerate.CurrencyChangeDetector;
import pl.coderslab.gatherer.exchangerate.ExchangeRateSnapshot;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyChangeDetectorTest {

    private CurrencyChangeDetector detector;
    private ExchangeRateProperties properties;
    private ExchangeRateSnapshot previousSnapshot;

    @BeforeEach
    void setUp() {
        detector = new CurrencyChangeDetector();
        properties = new ExchangeRateProperties();
        properties.setDefaultThresholdPercent(new BigDecimal("1.0"));
        properties.setCustomThresholds(Map.of(
                "PLN", new BigDecimal("0.5"),
                "EUR", new BigDecimal("2.0")
        ));
        previousSnapshot = new ExchangeRateSnapshot(
                "USD",
                Map.of(
                        "PLN", new BigDecimal("4.00"),
                        "EUR", new BigDecimal("0.90")
                ),
                Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    @Test
    void shouldReturnEmptyListWhenPreviousSnapshotMissing() {
        ExchangeRateSnapshot latestSnapshot = new ExchangeRateSnapshot(
                "USD",
                Map.of("PLN", new BigDecimal("4.05")),
                Instant.parse("2024-01-02T00:00:00Z")
        );

        List<CurrencyRateChangeMessage> result = detector.detectChanges(latestSnapshot, null, properties);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnChangeWhenThresholdExceeded() {
        ExchangeRateSnapshot latestSnapshot = new ExchangeRateSnapshot(
                "USD",
                Map.of("PLN", new BigDecimal("4.05"),
                        "EUR", new BigDecimal("0.89")),
                Instant.parse("2024-01-02T00:00:00Z")
        );

        List<CurrencyRateChangeMessage> result = detector.detectChanges(latestSnapshot, previousSnapshot, properties);

        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(message -> {
                    assertThat(message.currency()).isEqualTo("PLN");
                    assertThat(message.changePercentage()).isEqualByComparingTo("1.25");
                    assertThat(message.newRate()).isEqualByComparingTo("4.05");
                });
    }

    @Test
    void shouldIgnoreChangeBelowThreshold() {
        ExchangeRateSnapshot latestSnapshot = new ExchangeRateSnapshot(
                "USD",
                Map.of("EUR", new BigDecimal("0.905")),
                Instant.parse("2024-01-02T00:00:00Z")
        );

        List<CurrencyRateChangeMessage> result = detector.detectChanges(latestSnapshot, previousSnapshot, properties);

        assertThat(result).isEmpty();
    }
}
