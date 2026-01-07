package com.tgboyles.frugalfoxmcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import java.util.Objects;

@Configuration
@ConfigurationProperties(prefix = "frugalfox.api")
public class FrugalFoxApiConfig {

    private String baseUrl = "http://localhost:8080";
    private long timeout = 30000;

    @NonNull
    public String getBaseUrl() {
        return Objects.requireNonNull(baseUrl, "baseUrl must not be null");
    }

    public void setBaseUrl(@NonNull String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
