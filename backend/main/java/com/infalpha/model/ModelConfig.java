package com.infalpha.model;

/**
 * Runtime configuration for a specific model/provider.
 * Bound from application.yml per-provider section.
 */
public class ModelConfig {

    private boolean enabled;
    private String apiKey;
    private String baseUrl;
    private String defaultModel;
    private int timeoutSeconds = 30;
    private ProviderTier tier = ProviderTier.PAID;

    public ModelConfig() {}

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getDefaultModel() { return defaultModel; }
    public void setDefaultModel(String defaultModel) { this.defaultModel = defaultModel; }

    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }

    public ProviderTier getTier() { return tier; }
    public void setTier(ProviderTier tier) { this.tier = tier; }
}
