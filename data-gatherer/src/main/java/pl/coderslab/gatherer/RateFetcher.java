package pl.coderslab.gatherer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RateFetcher {

    @Scheduled(fixedRate = 60000) // co minutę
    public void fetchRates() {
        System.out.println("[DataGatherer] Fetching rates from API...");
    }
}
