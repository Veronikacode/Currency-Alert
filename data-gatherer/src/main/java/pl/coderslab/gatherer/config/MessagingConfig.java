package pl.coderslab.gatherer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    @Bean
    public TopicExchange gathererCurrencyAlertExchange(
            @Value("${app.messaging.exchange:currency.alerts.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue gathererCurrencyAlertQueue(
            @Value("${app.messaging.queue:currency.alerts.queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding gathererCurrencyAlertBinding(
            TopicExchange gathererCurrencyAlertExchange,
            Queue gathererCurrencyAlertQueue,
            @Value("${app.messaging.routing-key:currency.alerts.rate-change}") String routingKey) {
        return BindingBuilder.bind(gathererCurrencyAlertQueue)
                .to(gathererCurrencyAlertExchange)
                .with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter gathererMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}