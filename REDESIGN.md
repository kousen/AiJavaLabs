# AI Java Labs: Redesign Outline

Draft for markup, November 5, 2026. This is the plan, not the course. Nothing
in labs.md or slides.md changes until the shape here is agreed. Sections are
not timed on purpose: the instructor decides how long each one gets on the day.

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
| Same pattern, other vendors | OpenRouter catalog with no key, Anthropic's two headers, optional ElevenLabs | None |
| Ollama: your first chat request | New. `/api/chat` with a one-message list. `created_at` camel case fix folded in here | Replaces the `generate` arc. One sentence notes `/api/generate` exists and is Ollama-only |
| Ollama: every request is stateless | New. "My name is Ken." then "What's my name?" as two requests: the model does not know. Fix it by appending the assistant reply and asking again. The fix is visible as data | This is the hinge of the course. Nothing later makes sense without it |
| Ollama: streaming | `sendAsync` + `BodyHandlers.ofLines`, token consumer, SSE note for OpenAI | Streams `/api/chat`; chunks carry `message.content`. Recapture the wire-format slide |
| Ollama: vision (extension) | Image described for accessibility | Image goes on the chat message's `images` list. VERIFY against current Ollama API docs |
| Images (extension) | gpt-image-2.5-flare generation and edit | None |
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
| Chat through OpenRouter | `OpenAiChatModel` with the OpenRouter base URL, provider-prefixed ids, request logging step | None |
| Memory | New. Same "What's my name?" demo, now failing through the framework, then fixed with `MessageWindowChatMemory` on the AI Service. "The framework does the appending" | Memory currently appears only on the RAG slides |
| Many models at once | `MultiModelDemo`, virtual-thread fan-out, sequential vs concurrent timings | None |
| Streaming with lambdas (extension) | `LambdaStreamingResponseHandler`, blocking variants | None |
| Framework vs raw, side by side | `ApiComparisonDemo`: LangChain4j, raw chat completions, raw Responses API | None |

## Act 3: Close the loop (Java decides what happens next)

Why third: this is where the course meets what students are actually asked to
build now. Everything here is a request in a loop, and Java owns the loop.

| Section | Keeps from today | Changes |
|---|---|---|
| Tool calling | `@Tool` methods, the model picks, Java executes, wire-format slide, "When the Model Gets the Arguments Wrong" (defaults, the two error handlers, `ToolErrorVisibleToLlm`) | None. Scripted-model tests exist (Nov 5) |
| Structured output | Records with JSON Schema, `validate()` for what a schema cannot say | Optional step: a sealed result hierarchy with record variants and an exhaustive switch over model output. Data-oriented programming doing AI work, not a refactor. Do not argue adoption in class |
| Decision models | `DecisionModelDemo`, Jev through OpenRouter, probabilities drive thresholds in Java | Promoted from "Going Further" to a numbered section. Slide and lab in the same place |
| Where agents fit | The slide: an agent is the tool loop plus "what runs next" and "what each step sees"; primitives taught here; OperaGenerator for the four-way comparison; dated framework status lines | Moves from the Advanced tail to close the tools, structured output, and decisions cluster (after the decision-model slide), so every primitive in its table has been shown. The question comes up every delivery |
| RAG (extension) | `EasyRAGDemo`, three slides | Decide depth: Easy RAG only, or one raw embeddings call plus cosine similarity to match the course's premise. No lab exists today |

## Cross-cutting

- **Status codes**: `HttpSupport.requireSuccess` on every raw call; the 401 exercise in Act 1.
- **Testing**: every demo with a branch gets a scripted-model test that needs no key (the three LangChain4j demos have them). Live tests are tagged for cost and skip when their service is absent. A full build is expected green. A demo run is a happy-path check; the unhappy paths need a scripted model.
- **Cost**: tags, task names, cheap models by default. Unchanged.

## Durable vs dated

Lab text teaches only what will still be true in a year: the message list,
statelessness, status codes, the tool loop, output as data to validate.
Anything that moves lives on a slide that carries its date: model ids,
framework status (LangChain4j agentic, Embabel, Spring AI), pricing,
OpenRouter's catalog. The agents slide already does this. Make it the rule.

## Work plan, one commit each

1. Fix the labs.md table of contents (four sections missing today).
2. Move "Where Agents Fit" to follow the decision-model slide.
3. Ollama chat-first: `OllamaService` and `OllamaRecords` lose the `generate`
   shapes, `chat` and streaming over `/api/chat`, vision on a message; tests;
   labs rewritten; sealed-interfaces slide and wire-format streaming slide
   replaced. Largest item.
4. Statelessness steps: raw in Act 1, `MessageWindowChatMemory` in Act 2, one
   slide.
5. Promote decision models; renumber sections; label extensions.
6. Optional sealed-output step on structured output.
7. RAG depth decision, then the lab if one is wanted.
8. Update README, CLAUDE.md, and the pre-delivery checklist to the new shape.

## Open for Ken

- RAG depth (item 7).
- Whether "Same pattern, other vendors" stays in the main line or becomes an
  extension. It is cheap and supports the thesis, so the draft keeps it.
- Whether images stay at all. They do not touch the message list; the draft
  keeps them as an extension because the demo is popular.
