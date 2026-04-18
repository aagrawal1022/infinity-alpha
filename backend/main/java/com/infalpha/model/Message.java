package com.infalpha.model;

import jakarta.validation.constraints.NotBlank;

/**
 * A single message in a conversation (role + content).
 */
public class Message {

    @NotBlank(message = "Role must not be blank")
    private String role;     // "system", "user", "assistant"

    @NotBlank(message = "Content must not be blank")
    private String content;

    public Message() {}

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    // Builder
    public static MessageBuilder builder() { return new MessageBuilder(); }

    public static class MessageBuilder {
        private String role;
        private String content;

        public MessageBuilder role(String role) { this.role = role; return this; }
        public MessageBuilder content(String content) { this.content = content; return this; }
        public Message build() { return new Message(role, content); }
    }
}
