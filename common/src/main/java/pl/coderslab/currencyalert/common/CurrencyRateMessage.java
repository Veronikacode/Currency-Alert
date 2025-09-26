package pl.coderslab.currencyalert.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRateMessage implements Serializable {
    private String currencyCode;
    private Double rate;
    private LocalDateTime timestamp;
}
