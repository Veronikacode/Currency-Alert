package pl.coderslab.currencyalert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;
import pl.coderslab.currencyalert.common.messaging.CurrencyRateChangeMessage;
import pl.coderslab.provider.entity.CurrencyRate;
import pl.coderslab.provider.repository.CurrencyRateRepository;
import pl.coderslab.provider.service.CurrencyRateService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CurrencyRateServiceTest {

    @Mock
    private CurrencyRateRepository repository;

    @InjectMocks
    private CurrencyRateService service;

    @Captor
    private ArgumentCaptor<CurrencyRate> rateCaptor;

    @Captor
    private ArgumentCaptor<Sort> sortCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldPersistRateChangeFromMessage() {
        CurrencyRateChangeMessage message = new CurrencyRateChangeMessage(
                "PLN",
                "USD",
                new BigDecimal("4.5678"),
                new BigDecimal("1.25"),
                Instant.parse("2023-10-10T12:00:00Z")
        );

        when(repository.save(any(CurrencyRate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CurrencyRate result = service.recordRateChange(message);

        verify(repository).save(rateCaptor.capture());
        CurrencyRate persisted = rateCaptor.getValue();

        assertThat(persisted.getCurrencyCode()).isEqualTo("USD");
        assertThat(persisted.getRate()).isEqualByComparingTo("4.5678");
        assertThat(persisted.getTimestamp()).isEqualTo(OffsetDateTime.ofInstant(message.timestamp(), ZoneOffset.UTC));
        assertThat(result).isSameAs(persisted);
    }

    @Test
    void shouldReturnRatesSortedByTimestampDescending() {
        CurrencyRate rate = new CurrencyRate();
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(rate));

        List<CurrencyRate> result = service.getRecentRates();

        assertThat(result).containsExactly(rate);
        verify(repository).findAll(sortCaptor.capture());
        Sort sort = sortCaptor.getValue();
        assertThat(sort).isEqualTo(Sort.by(Sort.Direction.DESC, "timestamp"));
    }
}
