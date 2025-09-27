package pl.coderslab.provider.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.coderslab.provider.entity.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;

public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {

    Page<CurrencyRate> findByCurrencyCode(String currencyCode, Pageable pageable);

    Page<CurrencyRate> findByCurrencyCodeAndTimestampBetween(String currencyCode, OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    Page<CurrencyRate> findByCurrencyCodeAndTimestampGreaterThanEqual(String currencyCode, OffsetDateTime from, Pageable pageable);

    Page<CurrencyRate> findByCurrencyCodeAndTimestampLessThanEqual(String currencyCode, OffsetDateTime to, Pageable pageable);
}
