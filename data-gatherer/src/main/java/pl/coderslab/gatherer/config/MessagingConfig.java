package pl.coderslab.gatherer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    @Bean
    public TopicExchange gathererCurrencyAlertExchange(MessagingProperties properties) {
        return new TopicExchange(properties.getExchange());
    }

    @Bean
    public Queue gathererCurrencyAlertQueue(MessagingProperties properties) {
        return new Queue(properties.getQueue(), true);
    }

    @Bean
    public Binding gathererCurrencyAlertBinding(
            TopicExchange gathererCurrencyAlertExchange,
            Queue gathererCurrencyAlertQueue,
            MessagingProperties properties) {
        return BindingBuilder.bind(gathererCurrencyAlertQueue)
                .to(gathererCurrencyAlertExchange)
                .with(properties.getRoutingKey());
    }

    @Bean
    public Jackson2JsonMessageConverter gathererMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}