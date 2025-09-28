package pl.coderslab.gatherer.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import pl.coderslab.currencyalert.common.messaging.CurrencyRateChangeMessage;

@Component
public class CurrencyChangePublisher {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyChangePublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public CurrencyChangePublisher(RabbitTemplate rabbitTemplate,
                                   @Value("${app.messaging.exchange:currency.alerts.exchange}") String exchange,
                                   @Value("${app.messaging.routing-key:currency.alerts.rate-change}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publish(CurrencyRateChangeMessage message) {
        rabbitTemplate.convertAndSend(exchange,
                routingKey,
                message);
        logger.info("Published currency change message for {}/{}", message.baseCurrency(), message.currency());
    }
}