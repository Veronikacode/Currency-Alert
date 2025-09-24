package pl.coderslab.gatherer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DataGathererApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataGathererApplication.class, args);
    }
}