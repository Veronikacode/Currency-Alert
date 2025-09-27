package pl.coderslab.provider.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.coderslab.currencyalert.common.messaging.CurrencyRateChangeMessage;
import pl.coderslab.provider.entity.CurrencyRate;
import pl.coderslab.provider.repository.CurrencyRateRepository;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

/**
 * Encapsulates persistence logic for currency rate snapshots consumed from the messaging pipeline.
 */
@Service
@RequiredArgsConstructor
public class CurrencyRateService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "timestamp");

    private final CurrencyRateRepository repository;

    /**
     * Stores the incoming rate change information and returns the persisted entity.
     *
     * @param message event received from the messaging broker
     * @return persisted currency rate entity representing the incoming message
     */
    public CurrencyRate recordRateChange(CurrencyRateChangeMessage message) {
        CurrencyRate entity = new CurrencyRate();
        if (message.currency() != null) {
            entity.setCurrencyCode(message.currency().toUpperCase(Locale.ENGLISH));
        }
        entity.setRate(message.newRate());
        entity.setTimestamp(message.timestamp().atOffset(ZoneOffset.UTC));
        return repository.save(entity);
    }

    /**
     * Returns all persisted currency rates sorted from the newest to the oldest snapshot.
     *
     * @return list of persisted currency rates
     */
    public List<CurrencyRate> getRecentRates() {
        return repository.findAll(DEFAULT_SORT);
    }
}
