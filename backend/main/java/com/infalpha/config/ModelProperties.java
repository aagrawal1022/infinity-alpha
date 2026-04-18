package com.infalpha.config;

import com.infalpha.model.ModelConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Binds the 'inf-alpha' section from application.yml to POJOs.
 */
@ConfigurationProperties(prefix = "inf-alpha")
public class ModelProperties {

    /** Provider configs keyed by provider name: openai, claude, gemini, ollama */
    private Map<String, ModelConfig> providers;

    /** Retry settings */
    private RetryConfig retry = new RetryConfig();

    public Map<String, ModelConfig> getProviders() { return providers; }
    public void setProviders(Map<String, ModelConfig> providers) { this.providers = providers; }

    public RetryConfig getRetry() { return retry; }
    public void setRetry(RetryConfig retry) { this.retry = retry; }

    public static class RetryConfig {
        private int maxAttempts = 3;
        private long backoffMs = 1000;

        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

        public long getBackoffMs() { return backoffMs; }
        public void setBackoffMs(long backoffMs) { this.backoffMs = backoffMs; }
    }
}
