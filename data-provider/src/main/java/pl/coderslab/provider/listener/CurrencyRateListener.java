package pl.coderslab.provider.listener;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import pl.coderslab.currencyalert.common.messaging.CurrencyRateChangeMessage;
import pl.coderslab.provider.entity.CurrencyRate;
import pl.coderslab.provider.repository.CurrencyRateRepository;

import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class CurrencyRateListener {

    private static final Logger log = LoggerFactory.getLogger(CurrencyRateListener.class);

    private final CurrencyRateRepository repository;

    @RabbitListener(queues = "#{currencyAlertQueue.name}")
    public void receiveMessage(CurrencyRateChangeMessage message) {
        CurrencyRate entity = new CurrencyRate();
        entity.setCurrencyCode(message.currency());
        entity.setRate(message.newRate());
        entity.setTimestamp(message.timestamp().atOffset(ZoneOffset.UTC));

        repository.save(entity);
        log.debug("Stored rate update for {}", message.currency());
    }
}
