package pl.coderslab.currencyalert;

import org.junit.jupiter.api.Test;
import pl.coderslab.currencyalert.common.messaging.CurrencyRateChangeMessage;
import pl.coderslab.provider.entity.CurrencyRate;
import pl.coderslab.provider.listener.CurrencyRateListener;
import pl.coderslab.provider.notification.NotificationService;
import pl.coderslab.provider.service.CurrencyRateService;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CurrencyRateListenerTest {

    private final CurrencyRateService currencyRateService = mock(CurrencyRateService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final CurrencyRateListener listener = new CurrencyRateListener(currencyRateService, notificationService);

    @Test
    void shouldDelegateRateChangeToServiceAndNotifications() {
        CurrencyRateChangeMessage message = new CurrencyRateChangeMessage(
                "PLN",
                "USD",
                new BigDecimal("4.1234"),
                new BigDecimal("0.75"),
                Instant.parse("2023-10-01T10:15:30Z")
        );
        CurrencyRate persisted = new CurrencyRate();
        when(currencyRateService.recordRateChange(message)).thenReturn(persisted);

        listener.receiveMessage(message);

        verify(currencyRateService).recordRateChange(message);
        verify(notificationService).handleRateChange(persisted, message);
    }
}
