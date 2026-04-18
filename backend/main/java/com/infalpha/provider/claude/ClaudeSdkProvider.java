package com.infalpha.provider.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.TextBlockParam;
import com.infalpha.model.*;
import com.infalpha.provider.LlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PAID provider — Uses the official Anthropic Java SDK.
 * Supports: claude-sonnet-4-20250514, claude-3-opus, claude-3-haiku, etc.
 */
@Component
@ProviderInfo(
        key = "claude",
        displayName = "Anthropic Claude",
        tier = ProviderTier.PAID,
        modelPrefixes = {"claude-"}
)
public class ClaudeSdkProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(ClaudeSdkProvider.class);

    private AnthropicClient client;
    private ModelConfig config;
    private boolean ready = false;

    @Override
    public void initialize(ModelConfig config) {
        this.config = config;
        if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
            this.client = AnthropicOkHttpClient.builder()
                    .apiKey(config.getApiKey())
                    .build();
            this.ready = true;
            log.info("[claude] SDK initialized");
        } else {
            log.warn("[claude] No API key provided, provider will not be ready");
        }
    }

    @Override
    public boolean isReady() { return ready; }

    @Override
    public ChatResponse chat(ChatRequest request) {
        long start = System.currentTimeMillis();

        // Extract system prompt (Anthropic handles it separately)
        Optional<String> systemPrompt = request.getMessages().stream()
                .filter(m -> "system".equals(m.getRole()))
                .map(Message::getContent)
                .findFirst();

        // Build messages (exclude system)
        List<MessageParam> messages = request.getMessages().stream()
                .filter(m -> !"system".equals(m.getRole()))
                .map(this::toSdkMessage)
                .collect(Collectors.toList());

        MessageCreateParams.Builder builder = MessageCreateParams.builder()
                .model(request.getModel())
                .messages(messages)
                .maxTokens(request.getMaxTokens().longValue())
                .temperature(request.getTemperature());

        systemPrompt.ifPresent(s ->
                builder.system(MessageCreateParams.System.ofTextBlockParams(
                        List.of(TextBlockParam.builder().text(s).build())
                ))
        );

        com.anthropic.models.messages.Message response = client.messages().create(builder.build());
        long latency = System.currentTimeMillis() - start;

        // Extract text content
        StringBuilder content = new StringBuilder();
        for (ContentBlock block : response.content()) {
            if (block.isText()) {
                content.append(block.asText().text());
            }
        }

        return ChatResponse.builder()
                .id(response.id())
                .model(request.getModel())
                .provider("claude")
                .tier(ProviderTier.PAID)
                .content(content.toString())
                .usage(Usage.builder()
                        .promptTokens((int) response.usage().inputTokens())
                        .completionTokens((int) response.usage().outputTokens())
                        .totalTokens((int) (response.usage().inputTokens() + response.usage().outputTokens()))
                        .build())
                .latencyMs(latency)
                .build();
    }

    @Override
    public Flux<String> streamChat(ChatRequest request) {
        return Flux.create(sink -> {
            try {
                Optional<String> systemPrompt = request.getMessages().stream()
                        .filter(m -> "system".equals(m.getRole()))
                        .map(Message::getContent)
                        .findFirst();

                List<MessageParam> messages = request.getMessages().stream()
                        .filter(m -> !"system".equals(m.getRole()))
                        .map(this::toSdkMessage)
                        .collect(Collectors.toList());

                MessageCreateParams.Builder builder = MessageCreateParams.builder()
                        .model(request.getModel())
                        .messages(messages)
                        .maxTokens(request.getMaxTokens().longValue())
                        .temperature(request.getTemperature());

                systemPrompt.ifPresent(s ->
                        builder.system(MessageCreateParams.System.ofTextBlockParams(
                                List.of(TextBlockParam.builder().text(s).build())
                        ))
                );

                client.messages().createStreaming(builder.build())
                        .stream()
                        .forEach(event -> {
                            if (event.isContentBlockDelta()) {
                                var delta = event.asContentBlockDelta().delta();
                                if (delta.isText()) {
                                    sink.next(delta.asText().text());
                                }
                            }
                        });
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    private MessageParam toSdkMessage(Message msg) {
        MessageParam.Role role = "assistant".equals(msg.getRole())
                ? MessageParam.Role.ASSISTANT
                : MessageParam.Role.USER;

        return MessageParam.builder()
                .role(role)
                .content(msg.getContent())
                .build();
    }
}
