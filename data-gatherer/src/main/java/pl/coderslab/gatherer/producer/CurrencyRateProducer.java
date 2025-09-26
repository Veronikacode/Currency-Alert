package pl.coderslab.gatherer.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import pl.coderslab.currencyalert.common.CurrencyRateMessage;


import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CurrencyRateProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendRate(String currency, Double rate) {
        CurrencyRateMessage message = new CurrencyRateMessage(
                currency,
                rate,
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend("currency_rates", message);
        System.out.println("📤 Sent: " + message);
    }
}

