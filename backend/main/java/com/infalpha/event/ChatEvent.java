package com.infalpha.event;

import com.infalpha.model.ChatRequest;
import com.infalpha.model.ChatResponse;
import java.time.Instant;

/**
 * Event emitted after a chat completion.
 * Used by the Observer pattern to decouple side-effects (memory, metrics)
 * from the core chat flow.
 */
public class ChatEvent {

    private ChatRequest request;
    private ChatResponse response;
    private Instant timestamp;

    public ChatEvent() {
        this.timestamp = Instant.now();
    }

    public ChatEvent(ChatRequest request, ChatResponse response, Instant timestamp) {
        this.request = request;
        this.response = response;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public ChatRequest getRequest() { return request; }
    public void setRequest(ChatRequest request) { this.request = request; }

    public ChatResponse getResponse() { return response; }
    public void setResponse(ChatResponse response) { this.response = response; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public static ChatEventBuilder builder() { return new ChatEventBuilder(); }

    public static class ChatEventBuilder {
        private ChatRequest request;
        private ChatResponse response;
        private Instant timestamp;

        public ChatEventBuilder request(ChatRequest v) { this.request = v; return this; }
        public ChatEventBuilder response(ChatResponse v) { this.response = v; return this; }
        public ChatEventBuilder timestamp(Instant v) { this.timestamp = v; return this; }
        public ChatEvent build() { return new ChatEvent(request, response, timestamp); }
    }
}
