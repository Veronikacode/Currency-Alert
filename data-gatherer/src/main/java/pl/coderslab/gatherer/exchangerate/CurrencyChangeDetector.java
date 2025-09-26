package pl.coderslab.gatherer.exchangerate;

import org.springframework.stereotype.Component;
import pl.coderslab.currencyalert.common.messaging.CurrencyRateChangeMessage;
import pl.coderslab.gatherer.config.ExchangeRateProperties;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class CurrencyChangeDetector {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    public List<CurrencyRateChangeMessage> detectChanges(ExchangeRateSnapshot latestSnapshot,
                                                         ExchangeRateSnapshot previousSnapshot,
                                                         ExchangeRateProperties properties) {
        if (latestSnapshot == null || previousSnapshot == null) {
            return List.of();
        }

        List<CurrencyRateChangeMessage> changes = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : latestSnapshot.rates().entrySet()) {
            String currency = entry.getKey();
            BigDecimal latestRate = entry.getValue();
            BigDecimal previousRate = previousSnapshot.rates().get(currency);
            if (latestRate == null || previousRate == null || BigDecimal.ZERO.compareTo(previousRate) == 0) {
                continue;
            }
            BigDecimal changePercent = latestRate.subtract(previousRate)
                    .divide(previousRate, 6, RoundingMode.HALF_UP)
                    .multiply(ONE_HUNDRED)
                    .abs();
            BigDecimal threshold = properties.thresholdFor(currency);
            if (changePercent.compareTo(threshold) >= 0) {
                changes.add(new CurrencyRateChangeMessage(
                        Objects.requireNonNullElse(latestSnapshot.baseCurrency(), previousSnapshot.baseCurrency()),
                        currency,
                        latestRate,
                        changePercent,
                        latestSnapshot.retrievedAt()
                ));
            }
        }
        return changes;
    }
}
