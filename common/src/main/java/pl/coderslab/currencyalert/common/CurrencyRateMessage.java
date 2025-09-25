package pl.coderslab.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRateMessage {
    private String currencyCode;
    private Double rate;
    private LocalDateTime timestamp;
}
