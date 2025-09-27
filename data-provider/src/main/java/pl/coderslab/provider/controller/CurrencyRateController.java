package pl.coderslab.provider.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.coderslab.provider.dto.CurrencyRateDto;
import pl.coderslab.provider.service.CurrencyRateQueryService;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rates")
@RequiredArgsConstructor
public class CurrencyRateController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String DEFAULT_PAGE_SIZE_VALUE = "20";

    private final CurrencyRateQueryService queryService;

    @GetMapping("/latest")
    public List<CurrencyRateDto> latest(@RequestParam(defaultValue = "10") int limit) {
        return queryService.fetchLatest(limit);
    }

    @GetMapping("/history")
    public Page<CurrencyRateDto> history(@RequestParam String currency,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = DEFAULT_PAGE_SIZE_VALUE) int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        return queryService.fetchHistory(currency, from, to, pageable);
    }
}
