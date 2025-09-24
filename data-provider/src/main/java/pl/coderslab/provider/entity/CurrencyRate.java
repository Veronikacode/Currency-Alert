package pl.coderslab.provider.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class CurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String currencyCode;
    private Double rate;
    private LocalDateTime timestamp;

    public CurrencyRate() {
    }

    public CurrencyRate(String currencyCode, Double rate, LocalDateTime timestamp) {
        this.currencyCode = currencyCode;
        this.rate = rate;
        this.timestamp = timestamp;
    }

    // getters & setters
}
