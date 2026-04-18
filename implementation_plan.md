# Infrastructure Refactoring: LangChain4j Migration

The objective of this work is to drastically simplify the codebase by removing raw SDK interactions (OpenAI, Anthropic, Gemini) and substituting them with **LangChain4j** standard interfaces `ChatLanguageModel` and `StreamingChatLanguageModel`.

## User Review Required
> [!IMPORTANT]  
> Please review this refactoring strategy. This will replace our manual implementations of Gemini, Anthropic, and OpenAI SDKs with LangChain4j adapters. Do you approve moving forward with replacing the `pom.xml` dependencies and refactoring the providers?

## Architecture Strategy

Our backend tiering and routing (`ModelRegistry`, `RetryableProvider`, `ChatController`, `POST /api/v1/chat`) will remain exactly the same because they are excellent. We are ONLY refactoring the code inside `com.infalpha.provider.*`.

We will keep the `LlmProvider` interface as the bridge between our REST controllers and LangChain4j.

## Proposed Changes

---

### Phase 1: Dependencies

#### [MODIFY] [pom.xml](file:///Users/abhagraw2/Documents/Learnings/inf-alpha/backend/pom.xml)
- **Remove:** `openai-java`, `anthropic-java`, `google-genai`
- **Add:**
  - `dev.langchain4j:langchain4j:0.35.0`
  - `dev.langchain4j:langchain4j-open-ai:0.35.0` (for OpenAI, Groq, GitHub Models)
  - `dev.langchain4j:langchain4j-anthropic:0.35.0`
  - `dev.langchain4j:langchain4j-google-ai-gemini:0.35.0`

### Phase 2: Refactoring Providers

Instead of maintaining 8 completely different implementations of JSON parsing and HTTP streaming, we will convert every provider to be a thin wrapper around a LangChain4j model.

#### [MODIFY] [OpenAiSdkProvider.java](file:///Users/abhagraw2/Documents/Learnings/inf-alpha/backend/main/java/com/infalpha/provider/openai/OpenAiSdkProvider.java)
- Replace raw OpenAI Client API with `OpenAiChatModel.builder()...`
- Use `StreamingResponseHandler` wrapped in a `Flux.create(...)` sink to achieve perfect SSE streaming without manual byte chunking.

#### [MODIFY] [ClaudeSdkProvider.java](file:///Users/abhagraw2/Documents/Learnings/inf-alpha/backend/main/java/com/infalpha/provider/claude/ClaudeSdkProvider.java)
- Replace raw Anthropic SDK with `AnthropicChatModel.builder()...`

#### [MODIFY] [GeminiSdkProvider.java](file:///Users/abhagraw2/Documents/Learnings/inf-alpha/backend/main/java/com/infalpha/provider/gemini/GeminiSdkProvider.java)
- Replace raw GenAI SDK with `GeminiChatModel.builder()...`

*(We will apply similar thin wrappers to Groq, Github Models, and Gemini Free, reusing the `OpenAiChatModel` pointing to different base URLs!)*

## Verification Plan

### Automated Tests
1. Verify `mvn clean package` successfully strips all native SDK dependencies and compiles the new LangChain4j adapters.
2. Ensure Spring Boot boots on `localhost:8080`.

### Manual Verification
1. Run the Postman "Advanced Combo" collection on both `/chat` (Sync) and `/chat/stream` (Async) for OpenAI, Gemini, and Claude.
2. Confirm the resulting JSON and Server-Sent Events output formats exactly match the previous custom implementations so the API remains 100% backward-compatible.
