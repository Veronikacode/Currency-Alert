package pl.coderslab.gatherer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import pl.coderslab.gatherer.config.ExchangeRateProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(ExchangeRateProperties.class)
public class DataGathererApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataGathererApplication.class, args);
    }
}