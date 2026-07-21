# 30 Days of Agentic AI in Java

Building one agentic AI concept per day with **Spring AI 2.0** and **Spring Boot 4** — real code, no theory dumps.

| Day | Topic | Status |
|-----|-------|--------|
| 1 | AI chat endpoint with ChatClient | ✅ Done |
| 2 | Structured output — typed Java objects from the LLM | ✅ Done |
| 3 | Prompt templates — externalized, versioned prompts | 🔜 |

---

## Stack

- Java 21
- Spring Boot 4.1.0
- Spring AI 2.0.0 (`spring-ai-starter-model-openai`)
- Groq (free tier) serving Llama 3.3 70B — swappable for OpenAI/Anthropic/Ollama via config only

## Setup

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

## Run

```powershell
.\mvnw.cmd spring-boot:run   # Windows
./mvnw spring-boot:run       # Linux / macOS
```

---

## Day 1 — Chat Endpoint (`ChatController`)

A REST endpoint that talks to an LLM in ~35 lines. Spring AI auto-configures a `ChatClient.Builder` from `application.yml`; `.prompt().user(msg).call().content()` returns the model's text.

### Test

```powershell
curl.exe -X POST http://localhost:8080/api/chat -H "Content-Type: application/json" -d '{\"message\": \"Explain dependency injection in one sentence.\"}'
```

```json
{"reply": "Dependency injection is a design pattern where components receive their dependencies..."}
```

---

## Day 2 — Structured Output (`MeetingController`)

The LLM returns **typed Java objects** instead of raw strings. Define the target shape as records:

```java
public record ActionItem(String task, String owner, String deadline) {}

public record MeetingSummary(
        String title,
        List<ActionItem> actionItems,
        List<String> decisions) {}
```

Then swap `.content()` for `.entity()`:

```java
.call().entity(MeetingSummary.class)
```

Spring AI generates a JSON schema from the records, instructs the model to conform, and deserializes the reply — nested structures included. No manual JSON parsing.

### Test

Multiline JSON in PowerShell is an escaping nightmare — put the payload in a file:

```powershell
'{"notes": "Team sync 14 July. Priya will update the API docs by Thursday. Ashok to fix the login timeout bug before Friday release. We decided to delay the mobile launch to August and drop IE11 support."}' | Out-File -Encoding utf8 notes.json

curl.exe -X POST http://localhost:8080/api/meeting/extract -H "Content-Type: application/json" -d "@notes.json"
```

```json
{
  "title": "Team sync 14 July",
  "actionItems": [
    {"task": "update the API docs", "owner": "Priya", "deadline": "Thursday"},
    {"task": "fix the login timeout bug", "owner": "Ashok", "deadline": "Friday"}
  ],
  "decisions": ["delay the mobile launch to August", "drop IE11 support"]
}
```

**Why it matters:** enterprise systems consume typed data, not prose. `.entity()` is the bridge between an LLM and a database, a Jira board, or any downstream API.

**Limitation to know:** if the model returns malformed JSON, `.entity()` throws. Production error handling for this comes later in the series.

---

## Debugging notes (real errors hit while building)

- **404 `Unknown request URL`** → base-url missing `/v1` (see note above).
- **401 `Invalid API Key`** → env variable not set in the terminal session that launched the app, or key wrapped in `${...}` in the yml (the `${}` syntax reads a variable *name*, it doesn't wrap a value).
- **PowerShell curl fails** → use `curl.exe`, not `curl` (which aliases to `Invoke-WebRequest`).
- **Multiline JSON with curl on Windows** → skip inline escaping; write the payload to a file and use `-d "@file.json"`.

---

*Follow the series on [LinkedIn](https://www.linkedin.com/) — new day, new pattern.*
