package com.infalpha.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Plugin annotation — marks a class as an LLM provider implementation.
 * <p>
 * The registry auto-discovers all Spring beans annotated with @ProviderInfo
 * and indexes them by key, tier, and model prefixes.
 * <p>
 * <b>To add a new provider, just:</b>
 * <ol>
 *   <li>Create a class implementing {@code LlmProvider}</li>
 *   <li>Annotate it with {@code @Component} and {@code @ProviderInfo}</li>
 *   <li>Add config in {@code application.yml}</li>
 * </ol>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProviderInfo {

    /** Unique provider key, must match the YAML config key (e.g., "openai", "groq") */
    String key();

    /** Human-readable display name */
    String displayName();

    /** Pricing tier */
    ProviderTier tier();

    /**
     * Model name prefixes this provider can handle.
     * Used for auto-routing: if a model name starts with any of these, route to this provider.
     */
    String[] modelPrefixes();
}
