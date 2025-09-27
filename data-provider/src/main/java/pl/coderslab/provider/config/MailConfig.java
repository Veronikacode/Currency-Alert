package pl.coderslab.provider.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender(MailProperties properties) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(properties.getHost());
        sender.setPort(properties.getPort());
        if (StringUtils.hasText(properties.getUsername())) {
            sender.setUsername(properties.getUsername());
        }
        if (StringUtils.hasText(properties.getPassword())) {
            sender.setPassword(properties.getPassword());
        }
        if (StringUtils.hasText(properties.getProtocol())) {
            sender.setProtocol(properties.getProtocol());
        }
        Charset defaultEncoding = properties.getDefaultEncoding();
        if (defaultEncoding != null) {
            sender.setDefaultEncoding(defaultEncoding.name());
        }
        Properties javaMailProperties = sender.getJavaMailProperties();
        if (!properties.getProperties().isEmpty()) {
            javaMailProperties.putAll(properties.getProperties());
        }
        return sender;
    }
}