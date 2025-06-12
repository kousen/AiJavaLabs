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

- Java 17 or higher
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
./gradlew test
```

**Note**: Some tests require external services (OpenAI API, Ollama) to be available and properly configured.

## Project Structure

```
src/
├── main/java/com/kousenit/
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
- **OpenAI**: GPT-4.1-nano for advanced conversations
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

- **LangChain4j**: 1.0.1 (AI framework)
- **Apache POI**: 5.4.1 (Document processing)
- **Gson**: 2.13.1 (JSON parsing)
- **JUnit**: 5.13.0 (Testing)
- **Spotless**: 7.0.4 (Code formatting)

## Lab Exercises

Detailed step-by-step exercises are available in [labs.md](labs.md), covering:

1. **Generate Audio from Text** - OpenAI TTS integration
2. **List OpenAI Models** - API exploration and model discovery
3. **Install and Use Ollama** - Local AI model setup
4. **Streaming Responses** - Real-time AI interactions
5. **Vision Requests** - Image analysis capabilities
6. **Conversation Management** - Multi-turn chat implementations
7. **Image Generation** - DALL-E integration

## Educational Goals

This repository serves as a practical guide for:
- Understanding AI API integration patterns
- Learning modern Java features (records, sealed interfaces, pattern matching)
- Implementing error handling for external services
- Working with different AI model types (text, vision, audio)
- Building production-ready AI applications

## Contributing

This is an educational repository. The code demonstrates various AI integration patterns and serves as reference implementations for learning purposes.

## License

MIT License - see [LICENSE](LICENSE) file for details.
