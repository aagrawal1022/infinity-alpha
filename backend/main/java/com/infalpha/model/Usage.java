package com.infalpha.model;

/**
 * Token usage statistics returned by the provider.
 */
public class Usage {

    private int promptTokens;
    private int completionTokens;
    private int totalTokens;

    public Usage() {}

    public Usage(int promptTokens, int completionTokens, int totalTokens) {
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
    }

    public int getPromptTokens() { return promptTokens; }
    public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }

    public int getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }

    public int getTotalTokens() { return totalTokens; }
    public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }

    public static UsageBuilder builder() { return new UsageBuilder(); }

    public static class UsageBuilder {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;

        public UsageBuilder promptTokens(int v) { this.promptTokens = v; return this; }
        public UsageBuilder completionTokens(int v) { this.completionTokens = v; return this; }
        public UsageBuilder totalTokens(int v) { this.totalTokens = v; return this; }
        public Usage build() { return new Usage(promptTokens, completionTokens, totalTokens); }
    }
}
