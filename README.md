# AI Java Labs

A comprehensive educational repository demonstrating how to integrate various AI tools and services with Java applications.

## Overview

This repository contains practical exercises and example implementations for working with AI APIs in Java, including:

- **OpenAI Services**: Text-to-speech, chat completions, image generation (GPT Image)
- **Ollama Integration**: Local AI model interactions with text and vision capabilities
- **LangChain4j**: Framework integration for AI-powered applications
- **Tool Calling**: Java methods exposed safely as model-callable tools
- **Retrieval-Augmented Generation (RAG)**: Document-based question answering

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle 8.4+ (included via wrapper)
- OpenAI API key (for OpenAI services)
- OpenRouter API key (for the LangChain4j multi-model demos)
- Ollama installation (for local AI models)

### Environment Setup

1. **OpenAI API Key**: Set your OpenAI API key as an environment variable:
   ```bash
   export OPENAI_API_KEY=your_openai_api_key_here
   ```

   OpenRouter API key, used by `QuickChatDemo` and `MultiModelDemo` (one key reaches OpenAI, Gemini, and other providers by model id):
   ```bash
   export OPENROUTER_API_KEY=your_openrouter_key_here
   ```

   Optional provider keys used by some demos/tests:
   ```bash
   export ELEVENLABS_API_KEY=your_elevenlabs_key_here    # ElevenLabsService (optional lab)
   ```

2. **Ollama Setup**: Install Ollama and download the required models:
   ```bash
   # Install Ollama from https://ollama.com
   ollama pull gemma4
   ```

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run only local tests (no external services required)
./gradlew testLocal

# Run cheap API tests (minimal cost)
./gradlew testCheap

# Run demo tests (for presentations)
./gradlew testDemo
```

**Note**: Some tests require external services (OpenAI API, Ollama) to be available and properly configured.

## Training Materials

### Presentation Slides

Comprehensive training slides are available in multiple formats:

- **Live deck:** <https://kousen.github.io/AiJavaLabs/> — rebuilt on every push to `main` by [`deploy-slides-pages.yml`](.github/workflows/deploy-slides-pages.yml)
- **Latest PDF:** <https://github.com/kousen/AiJavaLabs/releases/latest/download/ai-java-labs-slides.pdf> — attached to a rolling release by [`build-slides-pdf.yml`](.github/workflows/build-slides-pdf.yml)
- **[slides.md](slides.md)** - Slidev source presentation

To run or export the Slidev presentation:
```bash
npm install
npx slidev slides.md

# Export to PDF (requires Playwright)
npx slidev export slides.md
```

## Project Structure

```
src/
├── main/java/com/kousenit/
│   ├── demos/                     # Standalone demo classes for training
│   │   ├── QuickChatDemo.java
│   │   ├── ConversationDemo.java  # Statelessness, then MessageWindowChatMemory
│   │   ├── TextToSpeechDemo.java
│   │   ├── MultiModelDemo.java
│   │   ├── ToolCallingDemo.java
│   │   ├── StructuredOutputDemo.java
│   │   ├── RagUnderTheHoodDemo.java # Chunk, embed (raw call), cosine, prompt
│   │   ├── StreamingDemo.java
│   │   ├── DecisionModelDemo.java # Typed decisions with probabilities (experimental API)
│   │   └── ResponsesApiDemo.java  # Responses API with Jackson's tree model
│   ├── GptImageService.java       # GPT Image generation
│   ├── OpenRouterService.java     # OpenRouter model catalog (no key needed)
│   ├── ElevenLabsService.java     # ElevenLabs voices and text-to-speech (no Java SDK)
│   ├── HttpSupport.java           # Status-code checks shared by the raw services
│   ├── EasyRAGDemo.java           # RAG implementation example
│   ├── OllamaService.java         # Ollama AI service integration
│   ├── OpenAiService.java         # OpenAI API interactions
│   ├── TextToSpeechService.java   # Audio generation from text
│   └── *Records.java              # Data models for API interactions
└── test/java/com/kousenit/        # Comprehensive test suite
```

## Key Features

### 🎵 Text-to-Speech
Generate MP3 audio files from text using OpenAI's TTS models:
- Models: `tts-1`, `tts-1-hd`, `gpt-4o-mini-tts`
- Multiple voice options: alloy, ash, ballad, coral, echo, fable, nova, onyx, sage, shimmer

### 🤖 Chat Is a Stateless Request Carrying a Message List
The idea the whole course is built on, taught twice:
- **Raw**: `OllamaService.chat` posts a `messages` list to `/api/chat`; two requests forget your name, appending the reply fixes it
- **Framework**: `ConversationDemo` shows an AI Service forgetting, then `MessageWindowChatMemory` doing the appending for you
- **Models**: gpt-6-luna through OpenAI or OpenRouter, `openrouter/free` to watch routing happen, local gemma4 through Ollama

### 🖼️ Vision, the Way Developers Use It
An image rides along on a chat message:
- `OllamaMessage.user(text, imagePath)` Base64-encodes a local file onto the message
- The example is a screenshot of a stack trace with the question "what went wrong?", answered by gemma4

### 🎨 Image Generation
Create images from text descriptions using GPT Image:
- High-quality image generation
- Customizable size, quality, and style parameters

### 🛠️ Tool Calling
Expose Java methods as model-callable tools through LangChain4j:
- Annotate methods with `@Tool`
- Document parameters with `@P`
- Register tools through `AiServices`
- Choose what the model sees when arguments are missing or a tool throws (`@P(defaultValue)`, `ToolArgumentsErrorHandler`, `ToolExecutionErrorHandler`)
- Keep validation and side effects in Java application code

### 🧾 Structured Output
Extract Java records from unstructured text:
- Enable JSON Schema support in LangChain4j
- Return records from AI Service methods
- Validate parsed objects before using them
- Optional: return a sealed interface (`InquiryResult`) and dispatch on the model's choice with an exhaustive switch

### 📚 RAG, Under the Hood and Then Easy
Document question answering, built by hand first:
- `RagUnderTheHoodDemo`: chunk the text, one raw `/v1/embeddings` call per 100 chunks, cosine similarity in a loop, top chunks pasted into the prompt
- `EasyRAGDemo`: the same four steps through LangChain4j's ingestor, in-memory store, and content retriever

### 🔌 Services Without a Java SDK (extension)
The same HttpClient + records + Jackson pattern against vendors that ship no Java library:
- OpenRouter's model catalog with modalities and per-token pricing (no key required); the images lab uses it to find the current image model
- ElevenLabs voices and text-to-speech, the OpenAI audio lab with different headers

### 🧭 Decision Models (experimental)
Typed yes/no, choice, and scale answers with probabilities instead of generated text:
- LangChain4j `DecisionModel` API with `TypeSafeDecisionModel`
- TypeSafe's Jev model, reached through OpenRouter with the same key as the chat demos
- Demo: `DecisionModelDemo`

## Dependencies

- **LangChain4j**: 1.23.0 (AI framework, including the Easy RAG and TypeSafe decision-model modules)
- **Jackson**: 3.2.4 (`tools.jackson`, JSON data binding and tree model)
- **JUnit**: 6.1.3 and **AssertJ** 4.0.0-M1 (Testing)
- **Spotless**: 8.11.0 (Code formatting)
- **Playwright**: For Slidev export functionality

## Lab Exercises

Detailed step-by-step exercises are available in [labs.md](labs.md), in three acts. Sections marked as extensions can be skipped without affecting what follows.

**Act 1: Read the wire** (raw HTTP and JSON)
1. **Generate Audio from Text** - POST, binary body, and why a status code is not an exception
2. **List the OpenAI Models** - GET and JSON binding with records and `JsonNode`
3. **Same pattern, other vendors** (extension) - OpenRouter's catalog, ElevenLabs
4. **Ollama: your first chat request** - the `messages` list every vendor uses
5. **Every request is stateless** - "My name is Inigo Montoya." then "What's my name?", and the fix
6. **Streaming Results** - `sendAsync` + `ofLines`, including a thinking model's empty chunks
7. **Ask about an image** (optional) - a stack-trace screenshot on a chat message
8. **Generating Images** (extension) - find the current model in the catalog, then GPT Image

**Act 2: Let the framework build it** (LangChain4j)
9. **Call cloud models through OpenRouter** - `OpenAiChatModel`, request logging, `openrouter/free` routing, memory
10. **Clean Streaming with LangChain4j** (extension)

**Act 3: Close the loop** (Java decides what happens next)
11. **Tool Calling** - Java methods the model can call, and what happens when it gets the arguments wrong
12. **Structured Output** - validated records, and optionally a sealed result type
13. **Decision models** - a probability for every option, thresholds in Java
14. **RAG under the hood, then Easy RAG** (extension)
15. **Going Further** - agents, JSON parsing strategies

## JSON Parsing Approaches

The project demonstrates two JSON parsing strategies for working with AI APIs:

### Jackson 3 (`tools.jackson`)
- Records bound directly from API responses with `mapper.readValue(json, Record.class)`
- `SNAKE_CASE` naming strategy maps `created_at` to `createdAt` without annotations
- `JsonNode` tree model with JSON Pointer (`at("/output/0/type")`) for responses whose shape varies
- Immutable mappers built with `JsonMapper.builder()`, unchecked exceptions
- Same line Spring Boot 4 uses; LangChain4j's internal Jackson 2 coexists on the classpath

## Educational Goals

This repository serves as a practical guide for:
- Understanding AI API integration patterns
- Learning modern Java features (records, sealed interfaces, pattern matching)
- Implementing error handling for external services
- Working with different AI model types (text, vision, audio)
- Working with JSON parsing for new API integration
- Building production-ready AI applications

## Contributing

This is an educational repository. The code demonstrates various AI integration patterns and serves as reference implementations for learning purposes.

## License

MIT License - see [LICENSE](LICENSE) file for details.
