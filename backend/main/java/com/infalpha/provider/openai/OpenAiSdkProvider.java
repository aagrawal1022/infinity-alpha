package com.infalpha.provider.openai;

import com.infalpha.model.*;
import com.infalpha.provider.LlmProvider;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionAssistantMessageParam;
import com.openai.models.ChatCompletionCreateParams;
import com.openai.models.ChatCompletionMessageParam;
import com.openai.models.ChatCompletionSystemMessageParam;
import com.openai.models.ChatCompletionUserMessageParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PAID provider — Uses the official OpenAI Java SDK.
 * Supports: gpt-4o, gpt-4-turbo, o1, o3, o4-mini, etc.
 */
@Component
@ProviderInfo(
        key = "openai",
        displayName = "OpenAI",
        tier = ProviderTier.PAID,
        modelPrefixes = {"gpt-", "o1-", "o3-", "o4-"}
)
public class OpenAiSdkProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiSdkProvider.class);

    private OpenAIClient client;
    private ModelConfig config;
    private boolean ready = false;

    @Override
    public void initialize(ModelConfig config) {
        this.config = config;
        if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
            this.client = OpenAIOkHttpClient.builder()
                    .apiKey(config.getApiKey())
                    .build();
            this.ready = true;
            log.info("[openai] SDK initialized");
        } else {
            log.warn("[openai] No API key provided, provider will not be ready");
        }
    }

    @Override
    public boolean isReady() { return ready; }

    @Override
    public ChatResponse chat(ChatRequest request) {
        long start = System.currentTimeMillis();

        List<ChatCompletionMessageParam> messages = request.getMessages().stream()
                .map(this::toSdkMessage)
                .collect(Collectors.toList());

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(request.getModel())
                .messages(messages)
                .temperature(request.getTemperature())
                .maxCompletionTokens(request.getMaxTokens().longValue())
                .topP(request.getTopP())
                .build();

        ChatCompletion completion = client.chat().completions().create(params);
        long latency = System.currentTimeMillis() - start;

        String content = completion.choices().isEmpty() ? ""
                : completion.choices().get(0).message().content().orElse("");

        Usage usage = null;
        if (completion.usage().isPresent()) {
            var u = completion.usage().get();
            usage = Usage.builder()
                    .promptTokens((int) u.promptTokens())
                    .completionTokens((int) u.completionTokens())
                    .totalTokens((int) u.totalTokens())
                    .build();
        }

        return ChatResponse.builder()
                .id(completion.id())
                .model(request.getModel())
                .provider("openai")
                .tier(ProviderTier.PAID)
                .content(content)
                .usage(usage)
                .latencyMs(latency)
                .build();
    }

    @Override
    public Flux<String> streamChat(ChatRequest request) {
        return Flux.create(sink -> {
            try {
                List<ChatCompletionMessageParam> messages = request.getMessages().stream()
                        .map(this::toSdkMessage)
                        .collect(Collectors.toList());

                ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                        .model(request.getModel())
                        .messages(messages)
                        .temperature(request.getTemperature())
                        .maxCompletionTokens(request.getMaxTokens().longValue())
                        .topP(request.getTopP())
                        .build();

                client.chat().completions().createStreaming(params)
                        .stream()
                        .forEach(chunk -> {
                            if (!chunk.choices().isEmpty()) {
                                chunk.choices().get(0).delta().content()
                                        .ifPresent(sink::next);
                            }
                        });
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    private ChatCompletionMessageParam toSdkMessage(Message msg) {
        return switch (msg.getRole()) {
            case "system" -> ChatCompletionMessageParam.ofSystem(
                    ChatCompletionSystemMessageParam.builder()
                            .content(msg.getContent()).build());
            case "assistant" -> ChatCompletionMessageParam.ofAssistant(
                    ChatCompletionAssistantMessageParam.builder()
                            .content(msg.getContent()).build());
            default -> ChatCompletionMessageParam.ofUser(
                    ChatCompletionUserMessageParam.builder()
                            .content(msg.getContent()).build());
        };
    }
}
