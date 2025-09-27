package pl.coderslab.provider.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.coderslab.provider.dto.CurrencyRateDto;
import pl.coderslab.provider.entity.CurrencyRate;
import pl.coderslab.provider.repository.CurrencyRateRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrencyRateQueryService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "timestamp");

    private final CurrencyRateRepository repository;

    public List<CurrencyRateDto> fetchLatest(int limit) {
        int pageSize = Math.max(1, limit);
        Pageable pageable = PageRequest.of(0, pageSize, DEFAULT_SORT);
        return repository.findAll(pageable)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public Page<CurrencyRateDto> fetchHistory(String currency,
                                              OffsetDateTime from,
                                              OffsetDateTime to,
                                              Pageable pageable) {
        if (currency == null || currency.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency parameter is required");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter 'from' must be before 'to'");
        }

        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);
        String normalizedCurrency = currency.toUpperCase();

        Page<CurrencyRate> result;
        if (from != null && to != null) {
            result = repository.findByCurrencyCodeAndTimestampBetween(normalizedCurrency, from, to, sorted);
        } else if (from != null) {
            result = repository.findByCurrencyCodeAndTimestampGreaterThanEqual(normalizedCurrency, from, sorted);
        } else if (to != null) {
            result = repository.findByCurrencyCodeAndTimestampLessThanEqual(normalizedCurrency, to, sorted);
        } else {
            result = repository.findByCurrencyCode(normalizedCurrency, sorted);
        }

        return result.map(this::toDto);
    }

    private CurrencyRateDto toDto(CurrencyRate rate) {
        return new CurrencyRateDto(
                rate.getId(),
                rate.getCurrencyCode(),
                rate.getRate(),
                rate.getTimestamp()
        );
    }
}
