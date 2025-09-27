package pl.coderslab.provider.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.coderslab.provider.entity.CurrencyRate;
import pl.coderslab.provider.repository.CurrencyRateRepository;
import pl.coderslab.provider.service.CurrencyRateService;

import java.util.List;

@RestController
@RequestMapping("/rates")
@RequiredArgsConstructor
public class CurrencyRateController {

    private final CurrencyRateService currencyRateService;

    @GetMapping
    public List<CurrencyRate> getAllRates() {
        return currencyRateService.getRecentRates();
    }
}
