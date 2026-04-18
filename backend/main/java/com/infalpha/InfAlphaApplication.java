package com.infalpha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.infalpha.config.ModelProperties;

/**
 * Inf-Alpha — Unified LLM Gateway.
 * <p>
 * A single interface to interact with all major LLM providers:
 * OpenAI, Anthropic Claude, Google Gemini, and Ollama (local models).
 */
@SpringBootApplication
@EnableConfigurationProperties(ModelProperties.class)
public class InfAlphaApplication {

    public static void main(String[] args) {
        SpringApplication.run(InfAlphaApplication.class, args);
    }
}
