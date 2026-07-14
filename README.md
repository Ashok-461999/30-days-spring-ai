# 30 Days of Agentic AI in Java

Building one agentic AI concept per day with **Spring AI 2.0** and **Spring Boot 4** — real code, no theory dumps.

| Day | Topic | Status |
|-----|-------|--------|
| 1 | AI chat endpoint with ChatClient | ✅ Done |
| 2 | Structured output (typed Java objects from the LLM) | 🔜 |

---

## Day 1 — Spring AI Chat Endpoint

A REST endpoint that talks to an LLM in ~35 lines. Zero SDK boilerplate — Spring AI auto-configures the client from config.

### Stack

- Java 21
- Spring Boot 4.1.0
- Spring AI 2.0.0 (`spring-ai-starter-model-openai`)
- Groq (free tier) serving Llama 3.3 70B — swappable for OpenAI/Anthropic/Ollama via config only

### Setup

1. Get a free API key at [console.groq.com](https://console.groq.com) (API Keys → Create).
2. Set it as an environment variable — **never hardcode keys in config files**:

```powershell
# Windows (PowerShell) — note: session-scoped, re-set in new terminals
$env:GROQ_API_KEY="gsk_..."
```

```bash
# Linux / macOS
export GROQ_API_KEY="gsk_..."
```

3. `src/main/resources/application.yml` references the variable:

```yaml
spring:
  ai:
    openai:
      api-key: ${GROQ_API_KEY}
      base-url: https://api.groq.com/openai/v1
      chat:
        options:
          model: llama-3.3-70b-versatile
```

> Note the `/v1` in the base-url. Spring AI 2.0 uses the official OpenAI Java SDK, which appends `/chat/completions` directly to the base-url — omitting `/v1` yields a 404 from Groq.

### Run

```powershell
.\mvnw.cmd spring-boot:run   # Windows
./mvnw spring-boot:run       # Linux / macOS
```

### Test

```powershell
curl.exe -X POST http://localhost:8080/api/chat -H "Content-Type: application/json" -d '{\"message\": \"Explain dependency injection in one sentence.\"}'
```

Response:

```json
{"reply": "Dependency injection is a design pattern where components receive their dependencies..."}
```

### How it works

1. `spring-ai-starter-model-openai` auto-configures a `ChatClient.Builder` from `application.yml`.
2. `ChatClient` is Spring AI's provider-agnostic entry point — swap Groq for OpenAI, Anthropic, or a local Ollama model by changing config, not code.
3. `.prompt().user(msg).call().content()` sends the message and returns the model's text.

### Debugging notes (real errors hit while building this)

- **404 `Unknown request URL`** → base-url missing `/v1` (see note above).
- **401 `Invalid API Key`** → env variable not set in the terminal session that launched the app, or key wrapped in `${...}` in the yml (the `${}` syntax reads a variable *name*, it doesn't wrap a value).
- **PowerShell curl fails** → use `curl.exe`, not `curl` (which aliases to `Invoke-WebRequest`).

---

*Follow the series on [LinkedIn](https://www.linkedin.com/) — new day, new pattern.*