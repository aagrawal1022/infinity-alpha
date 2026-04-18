package com.infalpha.event;

/**
 * Observer Pattern — Listener interface for chat events.
 * <p>
 * Implementations can react to chat completions for side-effects
 * such as storing conversation memory, updating metrics, or
 * triggering downstream workflows.
 */
public interface ChatEventListener {

    /**
     * Called after a chat completion is returned to the client.
     *
     * @param event the chat event containing request and response
     */
    void onChatCompleted(ChatEvent event);
}
