# Inf-Alpha v2: Unified LLM Gateway

Inf-Alpha is a production-grade, Spring Boot-based unified LLM gateway providing a single set of APIs to interface with multiple AI providers (OpenAI, Claude, Gemini, Groq, local Ollama, etc.).

It features a **Tiered Architecture** (Free vs. Paid routing) and a **Plugin Auto-Discovery** pattern, allowing you to add new LLM providers by dropping in a single Java file without modifying the core proxy engine.

## Prerequisites

- **Java 21** or later
- (Optional) **Ollama** installed locally if you wish to use the local open-source models

## Getting Started

### 1. Configure API Keys

We use a `.env` file to manage all provider API Keys.

1. Navigate to the `backend` directory:
   ```bash
   cd backend
   ```
2. Open the `.env` file in the backend directory.
3. Replace the placeholder values with your actual API keys.
   *(Note: You do not need to fill out all the keys, only the ones for the providers you wish to use. Unconfigured providers will gracefully decline to initialize on startup).*

### 2. Run the Application

Once your `.env` file is ready, you can start the Spring Boot server directly using the Maven Wrapper.

1. Make sure you are inside the `backend` directory:
   ```bash
   cd backend
   ```
2. Export your `.env` file into the active shell and boot the app:
   ```bash
   # Export all variables defined in the .env file
   export $(grep -v '^#' .env | xargs)
   
   # Run the Spring Boot application
   ./mvnw spring-boot:run
   ```

The server will start up and run on `http://localhost:8080`.

### 3. Test the Endpoints

Included in the root directory of this repository is a Postman collection:
`Inf-Alpha.postman_collection.json`

1. Open Postman.
2. Click **Import** and select the collection file.
3. Try hitting the different endpoints!

## API Examples

**View All Available Models**
```bash
curl http://localhost:8080/api/v1/models
```

**Send a Chat Request (Auto-routed)**
```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "model": "gpt-4o",
    "tier": "PAID",
    "messages": [
      {
        "role": "user",
        "content": "Hello, world!"
      }
    ]
}'
```

## Adding New Providers

Thanks to the plugin architecture, adding a new provider takes zero changes to the core code.
Simply create a new class implementing `LlmProvider` and annotate it with `@ProviderInfo`:

```java
@Component
@ProviderInfo(
    key = "my-new-provider",
    displayName = "Awesome New AI",
    tier = ProviderTier.FREE,
    modelPrefixes = {"awesome-"}
)
public class AwesomeProvider implements LlmProvider { ... }
```
Then add its config under the `inf-alpha.providers` block in `application.yml`. The system will automatically scan it, initialize it, wrap it in retry metrics, and route to it when users request a model starting with `awesome-`!
