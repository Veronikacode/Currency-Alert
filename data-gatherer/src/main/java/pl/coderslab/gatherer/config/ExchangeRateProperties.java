package pl.coderslab.gatherer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ConfigurationProperties(prefix = "app.exchange-rate")
@Validated
public class ExchangeRateProperties {

    private String appId;
    private String baseUrl = "https://openexchangerates.org/api";
    private String latestEndpoint = "/latest.json";
    private String baseCurrency = "USD";
    private BigDecimal defaultThresholdPercent = BigDecimal.ONE;
    private Map<String, BigDecimal> customThresholds = new HashMap<>();

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getLatestEndpoint() {
        return latestEndpoint;
    }

    public void setLatestEndpoint(String latestEndpoint) {
        this.latestEndpoint = latestEndpoint;
    }

    public Optional<String> getBaseCurrency() {
        return Optional.ofNullable(baseCurrency);
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public BigDecimal getDefaultThresholdPercent() {
        return defaultThresholdPercent;
    }

    public void setDefaultThresholdPercent(BigDecimal defaultThresholdPercent) {
        this.defaultThresholdPercent = defaultThresholdPercent;
    }

    public Map<String, BigDecimal> getCustomThresholds() {
        return customThresholds;
    }

    public void setCustomThresholds(Map<String, BigDecimal> customThresholds) {
        this.customThresholds = Optional.ofNullable(customThresholds)
                .map(HashMap::new)
                .orElseGet(HashMap::new);
    }

    public BigDecimal thresholdFor(String currencyCode) {
        if (currencyCode == null) {
            return defaultThresholdPercent;
        }
        return customThresholds.getOrDefault(currencyCode.toUpperCase(), defaultThresholdPercent);
    }
}
