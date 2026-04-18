package com.infalpha.controller;

import com.infalpha.model.ChatRequest;
import com.infalpha.model.ChatResponse;
import com.infalpha.model.ProviderTier;
import com.infalpha.service.ChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * REST Controller — Exposes the unified LLM gateway API.
 * <p>
 * All endpoints are provider-agnostic. The "model" field in the request
 * determines which LLM provider handles the call. Optionally, the "tier"
 * field restricts routing to FREE or PAID providers.
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Synchronous chat completion.
     * POST /api/v1/chat
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Streaming chat completion via Server-Sent Events.
     * POST /api/v1/chat/stream
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@Valid @RequestBody ChatRequest request) {
        return chatService.streamChat(request);
    }

    /**
     * List all available models and providers (grouped by tier).
     * GET /api/v1/models
     */
    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> listModels() {
        return ResponseEntity.ok(chatService.listModels(null));
    }

    /**
     * List free-tier models/providers only.
     * GET /api/v1/models/free
     */
    @GetMapping("/models/free")
    public ResponseEntity<Map<String, Object>> listFreeModels() {
        return ResponseEntity.ok(chatService.listModels(ProviderTier.FREE));
    }

    /**
     * List paid-tier models/providers only.
     * GET /api/v1/models/paid
     */
    @GetMapping("/models/paid")
    public ResponseEntity<Map<String, Object>> listPaidModels() {
        return ResponseEntity.ok(chatService.listModels(ProviderTier.PAID));
    }

    /**
     * Health check.
     * GET /api/v1/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "inf-alpha",
                "version", "2.0.0",
                "description", "Unified LLM Gateway (Free + Paid)"
        ));
    }
}
