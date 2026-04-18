package com.infalpha.memory;

import com.infalpha.event.ChatEvent;
import com.infalpha.event.ChatEventListener;
import com.infalpha.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory conversation store.
 * <p>
 * Implements the Observer pattern (ChatEventListener) to automatically
 * persist messages after each chat completion. Also provides lookup
 * to prepend conversation history to new requests.
 */
@Component
public class ConversationMemory implements ChatEventListener {

    private static final Logger log = LoggerFactory.getLogger(ConversationMemory.class);

    /** conversationId → ordered list of messages */
    private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();

    /**
     * Retrieve conversation history for a given conversation ID.
     */
    public List<Message> getHistory(String conversationId) {
        if (conversationId == null) {
            return Collections.emptyList();
        }
        return conversations.getOrDefault(conversationId, Collections.emptyList());
    }

    /**
     * Append messages to a conversation.
     */
    public void addMessages(String conversationId, List<Message> messages) {
        if (conversationId == null) return;
        conversations.computeIfAbsent(conversationId, k -> Collections.synchronizedList(new ArrayList<>()))
                .addAll(messages);
        log.debug("Stored {} message(s) for conversation {}", messages.size(), conversationId);
    }

    /**
     * Clear a specific conversation.
     */
    public void clearConversation(String conversationId) {
        conversations.remove(conversationId);
    }

    /**
     * Observer callback — auto-stores user message + assistant response.
     */
    @Override
    public void onChatCompleted(ChatEvent event) {
        String convId = event.getRequest().getConversationId();
        if (convId == null) return;

        List<Message> toStore = new ArrayList<>();
        List<Message> requestMessages = event.getRequest().getMessages();
        if (!requestMessages.isEmpty()) {
            Message lastUserMsg = requestMessages.get(requestMessages.size() - 1);
            toStore.add(lastUserMsg);
        }
        toStore.add(Message.builder()
                .role("assistant")
                .content(event.getResponse().getContent())
                .build());

        addMessages(convId, toStore);
    }

    /**
     * @return number of active conversations
     */
    public int conversationCount() {
        return conversations.size();
    }
}
