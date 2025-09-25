package pl.coderslab.provider.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.coderslab.provider.entity.CurrencyRate;
import pl.coderslab.provider.repository.CurrencyRateRepository;

import java.util.List;

@RestController
@RequestMapping("/rates")
@RequiredArgsConstructor
public class CurrencyRateController {

    private final CurrencyRateRepository repository;

    @GetMapping
    public List<CurrencyRate> getAllRates() {
        return repository.findAll();
    }
}
