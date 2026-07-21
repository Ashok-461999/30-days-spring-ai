# 30 Days of Agentic AI in Java

Building one agentic AI concept per day with **Spring AI 2.0** and **Spring Boot 4** — real code, no theory dumps.

| Day | Topic | Status |
|-----|-------|--------|
| 1 | AI chat endpoint with ChatClient | ✅ Done |
| 2 | Structured output — typed Java objects from the LLM | ✅ Done |
| 3 | Prompt templates — externalized, versioned prompts | ✅ Done |

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

## Day 3 — Prompt Templates (`PromptController`)

Prompts move out of Java and into **versioned `.st` files** on the classpath. The template is loaded once at startup and its `{placeholders}` are filled per request.

The template — `src/main/resources/prompts/summarize.st`:

```
Summarize the following text in {sentenceCount} sentences for an audience of {audience}.

Return only the summary. Do not include any preamble, labels, or explanation.

Text:
{input}
```

The controller loads it once, then injects request values:

```java
public PromptController(ChatClient.Builder builder,
        @Value("classpath:prompts/summarize.st") Resource summarizePrompt) {
    this.chatClient = builder.build();
    this.summarizeTemplate = new PromptTemplate(summarizePrompt);
}

@PostMapping("/api/summarize")
public SummarizeResponse summarize(@RequestBody SummarizeRequest req) {
    Prompt prompt = summarizeTemplate.create(Map.of(
            "input", req.text(),
            "sentenceCount", req.sentenceCount(),
            "audience", req.audience()));

    String reply = chatClient.prompt(prompt).call().content();
    return new SummarizeResponse(reply);
}
```

`sentenceCount` and `audience` are inputs *you* send to shape the summary's length and tone. The model obeys them.

### Test

```powershell
'{"text": "Spring AI lets Java apps talk to LLMs. It handles prompts, structured output, and works with OpenAI, Groq, and Ollama. Setup is just config.", "sentenceCount": 1, "audience": "executives"}' | Out-File -Encoding utf8 summarize.json

curl.exe -X POST http://localhost:8080/api/summarize -H "Content-Type: application/json" -d "@summarize.json"
```

```json
{"summary": "Spring AI is a Java framework that connects applications to large language models across multiple providers with minimal configuration."}
```

Change only the knobs — same input text — and the output shifts.

**Why it matters:** prompts become data, not code. Wording, tone, and length are tuned in a `.st` file — diffable in git — without recompiling.

---

## Debugging notes (real errors hit while building)

- **404 `Unknown request URL`** → base-url missing `/v1` (see note above).
- **401 `Invalid API Key`** → env variable not set in the terminal session that launched the app, or key wrapped in `${...}` in the yml (the `${}` syntax reads a variable *name*, it doesn't wrap a value).
- **PowerShell curl fails** → use `curl.exe`, not `curl` (which aliases to `Invoke-WebRequest`).
- **Multiline JSON with curl on Windows** → skip inline escaping; write the payload to a file and use `-d "@file.json"`.
- **500 mentioning `summarize.st` / `Resource`** → template file not at `src/main/resources/prompts/summarize.st`, or the app was built before the file was added. Restart `spring-boot:run`.
- **Model prepends `"Here is a 3-sentence summary..."`** → chatty models add framing when the prompt doesn't forbid it. Add a "Return only the summary" line to the `.st` file — fixed in data, no recompile.

---

*Follow the series on [LinkedIn](https://www.linkedin.com/) — new day, new pattern.*