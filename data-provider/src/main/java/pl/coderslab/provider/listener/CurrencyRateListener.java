package pl.coderslab.provider.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import pl.coderslab.provider.dto.CurrencyRateMessage;
import pl.coderslab.provider.entity.CurrencyRate;
import pl.coderslab.provider.repository.CurrencyRateRepository;

@Service
@RequiredArgsConstructor
public class CurrencyRateListener {

    private final CurrencyRateRepository repository;

    @RabbitListener(queues = "currency_rates")
    public void receiveMessage(CurrencyRateMessage message) {
        CurrencyRate entity = new CurrencyRate();
        entity.setCurrencyCode(message.getCurrencyCode());
        entity.setRate(message.getRate());
        entity.setTimestamp(message.getTimestamp());

        repository.save(entity);
        System.out.println("💾 Saved rate: " + message);
    }
}
