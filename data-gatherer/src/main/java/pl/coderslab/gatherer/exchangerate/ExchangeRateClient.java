package pl.coderslab.gatherer.exchangerate;

public interface ExchangeRateClient {

    ExchangeRateSnapshot fetchLatestRates();
}
