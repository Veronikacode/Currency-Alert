package pl.coderslab.currencyalert;

import org.junit.jupiter.api.Test;
import pl.coderslab.currencyalert.common.messaging.CurrencyRateChangeMessage;
import pl.coderslab.provider.entity.CurrencyRate;
import pl.coderslab.provider.listener.CurrencyRateListener;
import pl.coderslab.provider.repository.CurrencyRateRepository;
import pl.coderslab.provider.service.CurrencyRateService;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

class CurrencyRateListenerTest {

    private final CurrencyRateService currencyRateService = mock(CurrencyRateService.class);
    private final CurrencyRateListener listener = new CurrencyRateListener(currencyRateService);

    @Test
    void shouldDelegateRateChangeToService() {
        CurrencyRateChangeMessage message = new CurrencyRateChangeMessage(
                "PLN",
                "USD",
                new BigDecimal("4.1234"),
                new BigDecimal("0.75"),
                Instant.parse("2023-10-01T10:15:30Z")
        );

        listener.receiveMessage(message);

        verify(currencyRateService).recordRateChange(message);
    }

    @Test
    void shouldSupportLegacyRepositoryConstructor() {
        CurrencyRateRepository repository = mock(CurrencyRateRepository.class);
        CurrencyRateListener legacyListener = new CurrencyRateListener(repository);
        CurrencyRateChangeMessage message = new CurrencyRateChangeMessage(
                "PLN",
                "USD",
                new BigDecimal("4.9876"),
                new BigDecimal("2.15"),
                Instant.parse("2023-10-01T11:15:30Z")
        );

        when(repository.save(any(CurrencyRate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        legacyListener.receiveMessage(message);

        verify(repository).save(any(CurrencyRate.class));
    }
}
