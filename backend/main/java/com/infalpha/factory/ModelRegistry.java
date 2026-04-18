package com.infalpha.factory;

import com.infalpha.exception.ModelNotFoundException;
import com.infalpha.model.ProviderDescriptor;
import com.infalpha.model.ProviderTier;
import com.infalpha.provider.LlmProvider;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry Pattern — Tier-aware provider lookup.
 * <p>
 * Indexes all configured providers by:
 * - Provider key (e.g., "openai", "groq")
 * - Model prefix (e.g., "gpt-" → openai, "llama-" → groq)
 * - Tier (FREE, PAID)
 * <p>
 * Supports routing: findByModel("gpt-4o") or findByModel("llama3", FREE)
 */
@Component
public class ModelRegistry {

    private static final Logger log = LoggerFactory.getLogger(ModelRegistry.class);

    private final List<LlmProvider> providers;
    private final Map<String, LlmProvider> byKey = new ConcurrentHashMap<>();
    private final Map<ProviderTier, List<LlmProvider>> byTier = new ConcurrentHashMap<>();

    public ModelRegistry(@Qualifier("configuredProviders") List<LlmProvider> providers) {
        this.providers = providers;
    }

    @PostConstruct
    public void init() {
        for (LlmProvider provider : providers) {
            ProviderDescriptor desc = provider.getDescriptor();
            byKey.put(desc.getKey(), provider);
            byTier.computeIfAbsent(desc.getTier(), k -> new ArrayList<>()).add(provider);
        }

        long freeCount = byTier.getOrDefault(ProviderTier.FREE, List.of()).size();
        long paidCount = byTier.getOrDefault(ProviderTier.PAID, List.of()).size();
        log.info("ModelRegistry ready: {} provider(s) — {} FREE, {} PAID",
                providers.size(), freeCount, paidCount);
    }

    /**
     * Find the best provider for a model name (any tier).
     */
    public LlmProvider findByModel(String modelName) {
        return providers.stream()
                .filter(p -> p.getDescriptor().matchesModel(modelName))
                .findFirst()
                .orElseThrow(() -> new ModelNotFoundException(
                        "No provider found for model: " + modelName));
    }

    /**
     * Find the best provider for a model name within a specific tier.
     */
    public LlmProvider findByModel(String modelName, ProviderTier tier) {
        List<LlmProvider> tierProviders = byTier.getOrDefault(tier, List.of());
        return tierProviders.stream()
                .filter(p -> p.getDescriptor().matchesModel(modelName))
                .findFirst()
                .orElseThrow(() -> new ModelNotFoundException(
                        "No " + tier + " provider found for model: " + modelName));
    }

    /**
     * Get a provider by its key.
     */
    public LlmProvider getByKey(String key) {
        LlmProvider provider = byKey.get(key);
        if (provider == null) {
            throw new ModelNotFoundException("No provider registered with key: " + key);
        }
        return provider;
    }

    /**
     * List all providers in a specific tier.
     */
    public List<LlmProvider> listByTier(ProviderTier tier) {
        return Collections.unmodifiableList(byTier.getOrDefault(tier, List.of()));
    }

    /**
     * List all registered providers.
     */
    public Collection<LlmProvider> allProviders() {
        return Collections.unmodifiableCollection(providers);
    }

    /**
     * Get descriptors for all providers, optionally filtered by tier.
     */
    public List<ProviderDescriptor> getDescriptors(ProviderTier tier) {
        var source = tier != null ? byTier.getOrDefault(tier, List.of()) : providers;
        return source.stream()
                .map(LlmProvider::getDescriptor)
                .collect(Collectors.toList());
    }
}
