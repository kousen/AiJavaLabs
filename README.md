# AI Java Labs

A comprehensive educational repository demonstrating how to integrate various AI tools and services with Java applications.

## Overview

This repository contains practical exercises and example implementations for working with AI APIs in Java, including:

- **OpenAI Services**: Text-to-speech, chat completions, image generation (DALL-E)
- **Ollama Integration**: Local AI model interactions with text and vision capabilities
- **LangChain4j**: Framework integration for AI-powered applications
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

2. **Ollama Setup**: Install Ollama and download the required models:
   ```bash
   # Install Ollama from https://ollama.com
   ollama pull gemma3
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

- **[slides.md](slides.md)** - Slidev source presentation (38 interactive slides)
- **[slides.pdf](slides.pdf)** - PDF export for handouts and reference
- **[slides.pptx](slides.pptx)** - PowerPoint format for compatibility

To run the interactive Slidev presentation:
```bash
npm install
npx slidev slides.md
```

## Project Structure

```
src/
├── main/java/com/kousenit/
│   ├── demos/                     # Standalone demo classes for training
│   │   ├── QuickChatDemo.java
│   │   ├── TextToSpeechDemo.java
│   │   ├── MultiModelDemo.java
│   │   ├── StreamingDemo.java
│   │   ├── ResponsesApiDemo.java  # New Responses API with Gson
│   │   └── ResponsesApiJacksonDemo.java # Responses API with Jackson
│   ├── DalleService.java          # DALL-E image generation
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
- Models: `tts-1`, `tts-1-hd`
- Multiple voice options: alloy, echo, fable, onyx, nova, shimmer

### 🤖 Chat Interactions
Support for both cloud and local AI models:
- **OpenAI**: gpt-5-nano for advanced conversations
- **Ollama**: Local gemma3 model for privacy-focused applications

### 🖼️ Vision Capabilities
Image analysis and description generation:
- Multimodal support with Ollama's moondream model
- Base64 image encoding for API transmission

### 🎨 Image Generation
Create images from text descriptions using DALL-E 3:
- High-quality image generation
- Customizable size, quality, and style parameters

### 📚 Document Processing
RAG implementation for document-based question answering:
- LangChain4j integration
- Vector store support for efficient document retrieval

## Dependencies

- **LangChain4j**: 1.4.0 (AI framework)
- **Apache POI**: 5.4.1 (Document processing)
- **Gson**: 2.13.1 (JSON parsing)
- **Jackson**: 2.18.2 (JSON parsing with JsonNode)
- **JUnit**: 5.13.0 (Testing)
- **Spotless**: 7.0.4 (Code formatting)
- **Playwright**: For Slidev export functionality

## Lab Exercises

Detailed step-by-step exercises are available in [labs.md](labs.md), covering:

1. **Generate Audio from Text** - OpenAI TTS integration
2. **List OpenAI Models** - API exploration and model discovery
3. **Install and Use Ollama** - Local AI model setup
4. **Streaming Responses** - Real-time AI interactions
5. **Vision Requests** - Image analysis capabilities
6. **Conversation Management** - Multi-turn chat implementations
7. **Image Generation** - DALL-E integration

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

Both approaches are shown side-by-side to help understand how to work with new APIs before framework support exists.

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
