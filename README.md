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
- Ollama installation (for local AI models)

### Environment Setup

1. **OpenAI API Key**: Set your OpenAI API key as an environment variable:
   ```bash
   export OPENAI_API_KEY=your_openai_api_key_here
   ```

   Optional provider keys used by some demos/tests:
   ```bash
   export GOOGLEAI_API_KEY=your_google_ai_key_here
   export PERPLEXITY_API_KEY=your_perplexity_key_here
   ```

2. **Ollama Setup**: Install Ollama and download the required models:
   ```bash
   # Install Ollama from https://ollama.com
   ollama pull gpt-oss
   ollama pull moondream  # For vision tasks
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

- **[slides.md](slides.md)** - Slidev source presentation
- **[slides.pdf](slides.pdf)** - PDF export for handouts and reference
- **[slides.pptx](slides.pptx)** - PowerPoint format for compatibility

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
│   │   ├── TextToSpeechDemo.java
│   │   ├── MultiModelDemo.java
│   │   ├── ToolCallingDemo.java
│   │   ├── StructuredOutputDemo.java
│   │   ├── StreamingDemo.java
│   │   ├── ResponsesApiDemo.java  # New Responses API with Gson
│   │   └── ResponsesApiJacksonDemo.java # Responses API with Jackson
│   ├── GptImageService.java       # GPT Image generation
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

### 🤖 Chat Interactions
Support for both cloud and local AI models:
- **OpenAI**: gpt-5-nano for advanced conversations
- **Ollama**: Local gpt-oss model (OpenAI's open-source model)

### 🖼️ Vision Capabilities
Image analysis and description generation:
- Multimodal support with Ollama's moondream model
- Base64 image encoding for API transmission

### 🎨 Image Generation
Create images from text descriptions using GPT Image:
- High-quality image generation
- Customizable size, quality, and style parameters

### 🛠️ Tool Calling
Expose Java methods as model-callable tools through LangChain4j:
- Annotate methods with `@Tool`
- Document parameters with `@P`
- Register tools through `AiServices`
- Keep validation and side effects in Java application code

### 🧾 Structured Output
Extract Java records from unstructured text:
- Enable JSON Schema support in LangChain4j
- Return records from AI Service methods
- Validate parsed objects before using them

### 📚 Document Processing
RAG implementation for document-based question answering:
- LangChain4j integration
- Vector store support for efficient document retrieval

## Dependencies

- **LangChain4j**: 1.15.0 (AI framework)
- **Google Gen AI SDK**: 1.55.0
- **Apache POI**: 5.5.1 (Document processing)
- **Gson**: 2.14.0 (JSON parsing)
- **Jackson**: 2.21.3 (JSON parsing with JsonNode)
- **JUnit**: 5.13.4 (Testing)
- **Spotless**: 7.2.1 (Code formatting)
- **Playwright**: For Slidev export functionality

## Lab Exercises

Detailed step-by-step exercises are available in [labs.md](labs.md), covering:

1. **Generate Audio from Text** - OpenAI TTS integration
2. **List OpenAI Models** - API exploration and model discovery
3. **Install and Use Ollama** - Local AI model setup
4. **Streaming Responses** - Real-time AI interactions
5. **Vision Requests** - Image analysis capabilities
6. **Conversation Management** - Multi-turn chat implementations
7. **Tool Calling** - Let the model call Java methods
8. **Structured Output** - Extract validated Java records
9. **Image Generation** - GPT Image integration

## JSON Parsing Approaches

The project demonstrates two JSON parsing strategies for working with AI APIs:

### Gson (Google's JSON library)
- Tree-based navigation with `JsonElement`, `JsonObject`, and `JsonArray`
- Used in most service classes and `ResponsesApiDemo`
- Already integrated throughout the project

### Jackson (FasterXML)
- `JsonNode` API with JSON Pointer support (RFC 6901)
- Elegant path navigation: `root.at("/path/to/element")`
- Demonstrated in `ResponsesApiJacksonDemo`
- Spring Boot's default JSON processor

Both approaches are shown side-by-side to help understand how to work directly with APIs, debug framework behavior, and adopt features that may not map cleanly to a high-level abstraction yet.

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
