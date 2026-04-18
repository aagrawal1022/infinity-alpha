package com.infalpha.model;

import java.util.List;

/**
 * Runtime descriptor extracted from a {@link ProviderInfo} annotation.
 * Holds the metadata about a provider for registry indexing and API responses.
 */
public class ProviderDescriptor {

    private final String key;
    private final String displayName;
    private final ProviderTier tier;
    private final List<String> modelPrefixes;

    public ProviderDescriptor(String key, String displayName, ProviderTier tier, List<String> modelPrefixes) {
        this.key = key;
        this.displayName = displayName;
        this.tier = tier;
        this.modelPrefixes = modelPrefixes;
    }

    /**
     * Extract descriptor from a @ProviderInfo annotation.
     */
    public static ProviderDescriptor from(ProviderInfo info) {
        return new ProviderDescriptor(
                info.key(),
                info.displayName(),
                info.tier(),
                List.of(info.modelPrefixes())
        );
    }

    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }
    public ProviderTier getTier() { return tier; }
    public List<String> getModelPrefixes() { return modelPrefixes; }

    /**
     * Check if this provider supports a given model name by prefix matching.
     */
    public boolean matchesModel(String modelName) {
        if (modelName == null) return false;
        String lower = modelName.toLowerCase();
        return modelPrefixes.stream().anyMatch(prefix -> lower.startsWith(prefix.toLowerCase()));
    }
}
