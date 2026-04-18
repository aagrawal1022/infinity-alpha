package com.infalpha.provider.groq;

import com.infalpha.model.ProviderInfo;
import com.infalpha.model.ProviderTier;
import com.infalpha.provider.common.BaseOpenAiCompatibleProvider;
import org.springframework.stereotype.Component;

/**
 * FREE provider — Groq (ultra-fast inference).
 * <p>
 * Uses the OpenAI SDK pointed at Groq's OpenAI-compatible endpoint.
 * Auth: Groq API key (free signup, no credit card required).
 * Free models: Llama 3.3 70B, Llama 4 Scout, Mixtral, Gemma, etc.
 * <p>
 * Config:
 * - base-url: https://api.groq.com/openai
 * - api-key: ${GROQ_API_KEY}
 */
@Component
@ProviderInfo(
        key = "groq",
        displayName = "Groq",
        tier = ProviderTier.FREE,
        modelPrefixes = {"llama-", "llama3", "llama4", "mixtral", "gemma", "deepseek"}
)
public class GroqProvider extends BaseOpenAiCompatibleProvider {
    // All logic inherited. Just metadata!
}
