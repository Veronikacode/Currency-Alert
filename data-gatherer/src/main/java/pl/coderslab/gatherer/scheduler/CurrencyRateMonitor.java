package pl.coderslab.gatherer.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.coderslab.currencyalert.common.messaging.CurrencyRateChangeMessage;
import pl.coderslab.gatherer.config.ExchangeRateProperties;
import pl.coderslab.gatherer.exchangerate.CurrencyChangeDetector;
import pl.coderslab.gatherer.exchangerate.ExchangeRateClient;
import pl.coderslab.gatherer.exchangerate.ExchangeRateSnapshot;
import pl.coderslab.gatherer.messaging.CurrencyChangePublisher;

import java.util.List;

@Component
public class CurrencyRateMonitor {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyRateMonitor.class);

    private final ExchangeRateClient exchangeRateClient;
    private final CurrencyChangeDetector changeDetector;
    private final CurrencyChangePublisher publisher;
    private final ExchangeRateProperties exchangeRateProperties;

    private ExchangeRateSnapshot lastSnapshot;

    public CurrencyRateMonitor(ExchangeRateClient exchangeRateClient,
                               CurrencyChangeDetector changeDetector,
                               CurrencyChangePublisher publisher,
                               ExchangeRateProperties exchangeRateProperties) {
        this.exchangeRateClient = exchangeRateClient;
        this.changeDetector = changeDetector;
        this.publisher = publisher;
        this.exchangeRateProperties = exchangeRateProperties;
    }

    @Scheduled(cron = "${app.scheduling.fetch-cron:0 0 * * * *}")
    public void pollExchangeRates() {
        try {
            ExchangeRateSnapshot latestSnapshot = exchangeRateClient.fetchLatestRates();
            if (lastSnapshot == null) {
                logger.info("Received initial exchange rate snapshot at {}", latestSnapshot.retrievedAt());
                lastSnapshot = latestSnapshot;
                return;
            }

            List<CurrencyRateChangeMessage> changes = changeDetector.detectChanges(latestSnapshot, lastSnapshot,
                    exchangeRateProperties);
            changes.forEach(publisher::publish);
            if (changes.isEmpty()) {
                logger.debug("No significant currency changes detected");
            }
            lastSnapshot = latestSnapshot;
        } catch (Exception ex) {
            logger.error("Failed to process exchange rates", ex);
        }
    }
}