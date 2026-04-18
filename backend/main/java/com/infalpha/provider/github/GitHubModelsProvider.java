package com.infalpha.provider.github;

import com.infalpha.model.ProviderInfo;
import com.infalpha.model.ProviderTier;
import com.infalpha.provider.common.BaseOpenAiCompatibleProvider;
import org.springframework.stereotype.Component;

/**
 * FREE provider — GitHub Models (Azure AI Inference).
 * <p>
 * Uses the OpenAI SDK pointed at GitHub's Azure endpoint.
 * Auth: GitHub Personal Access Token (free, no credit card).
 * Free models: GPT-4o-mini, Llama, Phi, Mistral, etc.
 * <p>
 * Config:
 * - base-url: https://models.inference.ai.azure.com
 * - api-key: ${GITHUB_TOKEN}
 */
@Component
@ProviderInfo(
        key = "github-models",
        displayName = "GitHub Models",
        tier = ProviderTier.FREE,
        modelPrefixes = {"gpt-4o-mini", "openai/", "meta/", "microsoft/", "mistral/"}
)
public class GitHubModelsProvider extends BaseOpenAiCompatibleProvider {
    // All logic inherited from BaseOpenAiCompatibleProvider.
    // Just adding metadata via @ProviderInfo — that's the plugin model!
}
