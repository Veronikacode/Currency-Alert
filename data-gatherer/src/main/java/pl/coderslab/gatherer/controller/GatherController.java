package pl.coderslab.gatherer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.coderslab.gatherer.producer.CurrencyRateProducer;

@RestController
@RequestMapping("/gather")
@RequiredArgsConstructor
public class GatherController {

    private final CurrencyRateProducer producer;

    @PostMapping("/send")
    public String sendMockRate() {
        producer.sendRate("USD", 4.25);
        return "Mock rate sent!";
    }
}

