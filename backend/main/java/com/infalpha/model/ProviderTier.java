package com.infalpha.model;

/**
 * Defines the pricing tier for an LLM provider.
 * Used to segregate free and paid providers for routing.
 */
public enum ProviderTier {

    /** Free-to-use providers (GitHub Models, Groq, Google AI Studio, HuggingFace, Ollama) */
    FREE,

    /** Paid providers requiring billing accounts (OpenAI, Anthropic, Google Vertex) */
    PAID
}
