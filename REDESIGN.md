# AI Java Labs: Redesign Outline

Agreed November 5, 2026, after an eight-question review with Ken. This is the
plan the redesign branch implements. Sections are not timed on purpose: the
instructor decides how long each one gets on the day. Decisions are marked
**Decided** where Ken ruled.

## Thesis

Every call to an AI model is a stateless HTTP request carrying a list of
messages. The course starts by reading that request and response on the wire,
lets a framework build them, and then closes the loop: Java decides what the
model is asked next, with tools, typed outputs, and probabilities.

Three acts. Each one builds on the message list from the one before.

## Act 1: Read the wire (raw HTTP and JSON)

Why first: when the framework does something surprising, this is what you
look at. Frameworks churn; the wire does not.

| Section | Keeps from today | Changes |
|---|---|---|
| Text to speech | POST with a JSON body, binary response, `ofFile`, the bad-key exercise that shows a status code is not an exception | Output lands in `build/audio` (done Nov 5) |
| List the models | GET, Jackson 3, records when the shape is known, `JsonNode` when it varies | None |
| Same pattern, other vendors (extension) | OpenRouter catalog with no key, optional ElevenLabs | **Decided**: extension. The raw Anthropic call goes (issue 8). The catalog query returns in the images lab, filtered by `output_modalities=image`, as the way to find the current image model |
| Ollama: your first chat request | New. `/api/chat` with a one-message list. `created_at` camel case fix folded in here | Replaces the `generate` arc. **Decided**: `/api/generate` stays as a short aside with one runnable example, `generate(model, prompt)` and its test; the request-record hierarchy and the streaming and vision `generate` variants go |
| Ollama: every request is stateless | New. "My name is Inigo Montoya." then "What's my name?" as two requests: the model does not know. Fix it by appending the assistant reply and asking again. The fix is visible as data | **Decided**: done here raw and again in Act 2 through the framework. Fundamental and counter-intuitive, because the web site and the app hide it |
| Ollama: streaming | `sendAsync` + `BodyHandlers.ofLines`, token consumer, SSE note for OpenAI | Streams `/api/chat`; chunks carry `message.content`. Recapture the wire-format slide |
| Ollama: vision (extension) | Image on a chat message | **Decided**: the example becomes a developer's use, a screenshot of a stack trace with the model asked what went wrong, not "what do you see". Image goes on the chat message's `images` list. VERIFY the field against current Ollama API docs |
| Images (extension) | Generation and edit through `GptImageService` | **Decided**: stays as an extension, moves to gpt-image-2.6-flare (released Nov 4, 2026). The lab opens by finding the current model in the OpenRouter catalog with `output_modalities=image` |
| Responses API | JSON Pointer for fixed fields, a walk for the output array, why `/output/0` is wrong for reasoning models | None. This is the first place model output is a sum type; say so, it sets up Act 3 |

Deleted from Act 1: the `generate` labs (first request, both streaming
steps, vision, camel case as its own lab) and "Refactor to sealed
interfaces." The refactor lab's only subject was the two `generate` request
shapes. Records stay everywhere, unannounced; they are just how JSON binds.

## Act 2: Let the framework build it (LangChain4j)

Why second: students have seen the request, so the framework is now a
convenience they can see through, not magic.

| Section | Keeps from today | Changes |
|---|---|---|
| Chat through OpenRouter | `OpenAiChatModel` with the OpenRouter base URL, provider-prefixed ids, request logging step | New step: switch the model id to `openrouter/free` and watch which model answers. Shows routing without a lecture on vendor differences |
| Memory | New. Same Inigo Montoya demo, now failing through the framework, then fixed with `MessageWindowChatMemory` on the AI Service. "The framework does the appending" | **Decided**: both places. Memory currently appears only on the RAG slides |
| Many models at once | `MultiModelDemo`, virtual-thread fan-out, sequential vs concurrent timings | None |
| Streaming with lambdas (extension) | `LambdaStreamingResponseHandler`, blocking variants | None |
| Framework vs raw, side by side | `ApiComparisonDemo`: LangChain4j, raw chat completions, raw Responses API | None |

## Act 3: Close the loop (Java decides what happens next)

Why third: this is where the course meets what students are actually asked to
build now. Everything here is a request in a loop, and Java owns the loop.

| Section | Keeps from today | Changes |
|---|---|---|
| Tool calling | `@Tool` methods, the model picks, Java executes, wire-format slide, "When the Model Gets the Arguments Wrong" (defaults, the two error handlers, `ToolErrorVisibleToLlm`) | None. Scripted-model tests exist (Nov 5) |
| Structured output | Records with JSON Schema, `validate()` for what a schema cannot say | **Decided**: optional, cuttable step. The inquiry becomes a sealed `InquiryResult` with `QuoteRequest`, `SchedulingRequest`, and `Noise` variants; an exhaustive switch decides what the application does next. Data-oriented programming doing AI work, not a refactor. Do not argue adoption in class. Candidate for a later YouTube video or series |
| Decision models | `DecisionModelDemo`, Jev through OpenRouter, probabilities drive thresholds in Java | Promoted from "Going Further" to a numbered section. Slide and lab in the same place |
| Where agents fit | The slide: an agent is the tool loop plus "what runs next" and "what each step sees"; primitives taught here; OperaGenerator for the four-way comparison; dated framework status lines | Moves from the Advanced tail to close the tools, structured output, and decisions cluster (after the decision-model slide), so every primitive in its table has been shown. The question comes up every delivery |
| RAG (extension) | `EasyRAGDemo`, three slides | **Decided**: under the hood first. One raw embeddings call, cosine similarity in plain Java over a few chunks, top chunk into the prompt, then Easy RAG as "the framework does this". Fallback if it proves heavy: Easy RAG only. No lab exists today |

## Cross-cutting

- **Status codes**: `HttpSupport.requireSuccess` on every raw call; the 401 exercise in Act 1.
- **Testing**: every demo with a branch gets a scripted-model test that needs no key (the three LangChain4j demos have them). Live tests are tagged for cost and skip when their service is absent. A full build is expected green. A demo run is a happy-path check; the unhappy paths need a scripted model.
- **Cost**: tags, task names, cheap models by default. Unchanged.

## Removed integrations

**Decided**: the course needs OpenAI and OpenRouter keys, plus Ollama locally
and ElevenLabs optionally. Gemini is reached through OpenRouter. So:

- Remove the Google GenAI SDK dependency and `ImageGenerator` (Gemini image
  generation; undocumented in labs and slides).
- Remove the raw `AnthropicService` and its test.
- Keep the `langchain4j-anthropic` and `langchain4j-google-ai-gemini` modules
  on the classpath, and keep the Gemini test in `OpenAiLC4jTest` as the one
  pointer to them. It skips without `GOOGLEAI_API_KEY`.
- README, Prerequisites slide, and CLAUDE.md list only the keys above.

## Durable vs dated

Lab text teaches only what will still be true in a year: the message list,
statelessness, status codes, the tool loop, output as data to validate.
Anything that moves lives on a slide that carries its date: model ids,
framework status (LangChain4j agentic, Embabel, Spring AI), pricing,
OpenRouter's catalog. The agents slide already does this. Make it the rule.

## Work plan, one commit each

1. Fix the labs.md table of contents (four sections missing today). Done.
2. Move "Where Agents Fit" to follow the decision-model slide. Done.
3. Done. Ollama chat-first: `OllamaRecords` loses the sealed request hierarchy;
   `OllamaService` keeps `generate(model, prompt)` as the aside, gains chat
   streaming over `/api/chat` and vision on a message; tests; labs rewritten
   with the raw Inigo Montoya step; sealed-interfaces slide and wire-format
   streaming slide replaced. Largest item.
4. Done. Act 2 memory step with `MessageWindowChatMemory`, the `openrouter/free`
   routing step, one slide.
5. Done. Remove the Google SDK, `ImageGenerator`, `AnthropicService`; demote the
   other-vendors section; images lab to gpt-image-2.6-flare with the catalog
   step first; key lists trimmed everywhere.
6. Done. Promote decision models; renumber sections; label extensions.
7. Optional sealed `InquiryResult` step on structured output.
8. RAG under the hood, then Easy RAG; the lab.
9. Update README, CLAUDE.md, and the pre-delivery checklist to the new shape;
   draft the O'Reilly course description from the thesis and the three acts.

## Open

Nothing blocking. Verify during item 3 that Ollama's chat messages accept an
`images` list, and during item 5 that `gpt-image-2.6-flare` is served by the
OpenAI images endpoint as well as through OpenRouter.
