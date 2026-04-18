package com.infalpha.factory;

import com.infalpha.config.ModelProperties;
import com.infalpha.decorator.LoggingProvider;
import com.infalpha.decorator.RetryableProvider;
import com.infalpha.model.ModelConfig;
import com.infalpha.model.ProviderDescriptor;
import com.infalpha.model.ProviderInfo;
import com.infalpha.provider.LlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory Pattern + Plugin Auto-Discovery.
 * <p>
 * Scans the Spring context for all beans annotated with @ProviderInfo,
 * matches them with YAML config, initializes them, and wraps with decorators.
 * <p>
 * <b>Adding a new provider requires ZERO changes here.</b>
 * Just create a class with @Component + @ProviderInfo + implement LlmProvider.
 */
@Configuration
public class ModelFactory {

    private static final Logger log = LoggerFactory.getLogger(ModelFactory.class);

    private final ModelProperties properties;
    private final ApplicationContext context;

    public ModelFactory(ModelProperties properties, ApplicationContext context) {
        this.properties = properties;
        this.context = context;
    }

    /**
     * Auto-discovers all LlmProvider beans with @ProviderInfo,
     * initializes enabled ones, and wraps with retry + logging decorators.
     */
    @Bean
    public List<LlmProvider> configuredProviders() {
        List<LlmProvider> providers = new ArrayList<>();
        Map<String, ModelConfig> configs = properties.getProviders();
        var retryConfig = properties.getRetry();

        if (configs == null) {
            log.warn("No LLM providers configured in application.yml");
            return providers;
        }

        // Find all beans annotated with @ProviderInfo
        Map<String, LlmProvider> beans = context.getBeansOfType(LlmProvider.class);

        for (LlmProvider rawProvider : beans.values()) {
            // Skip decorators (they don't have @ProviderInfo)
            ProviderInfo info = rawProvider.getClass().getAnnotation(ProviderInfo.class);
            if (info == null) continue;

            String key = info.key();
            ModelConfig config = configs.get(key);

            if (config == null) {
                log.debug("⊘ No config for provider '{}', skipping", key);
                continue;
            }

            if (!config.isEnabled()) {
                log.info("⊘ Provider '{}' is disabled, skipping", key);
                continue;
            }

            // Initialize the provider with its config
            try {
                rawProvider.initialize(config);

                if (!rawProvider.isReady()) {
                    log.warn("⊘ Provider '{}' initialized but not ready (missing credentials?)", key);
                    continue;
                }

                // Wrap with decorators: retry → logging
                LlmProvider withRetry = new RetryableProvider(
                        rawProvider, retryConfig.getMaxAttempts(), retryConfig.getBackoffMs());
                LlmProvider withLogging = new LoggingProvider(withRetry);

                providers.add(withLogging);

                ProviderDescriptor desc = rawProvider.getDescriptor();
                log.info("✓ Registered provider: {} [{}] (tier={}, model={})",
                        desc.getDisplayName(), key, desc.getTier(), config.getDefaultModel());

            } catch (Exception e) {
                log.error("✗ Failed to initialize provider '{}': {}", key, e.getMessage());
            }
        }

        log.info("ModelFactory initialized {} provider(s)", providers.size());
        return providers;
    }
}
