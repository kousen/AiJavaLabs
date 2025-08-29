# AI Java Labs - Development Context

This file provides context for AI assistants working on the AI Java Labs project.

## Project Overview

**AI Java Labs** is an educational Java project demonstrating integration with various AI services and tools. It serves as a teaching repository with hands-on exercises for learning AI API integration patterns in Java.

## Current State

### Recent Updates (December 2024 / January 2025)
- ✅ Updated all AI models to latest versions (gemma2→gemma3, GPT models→gpt-4o-mini)
- ✅ Updated LangChain4j to 1.4.0 (major version upgrade)
- ✅ Configured Java 21 using Gradle's toolchain support
- ✅ Refactored HttpClient usage from try-with-resources to static class attributes
- ✅ Created standalone demo classes for training presentations
- ✅ Implemented test categorization with JUnit tags for cost management
- ✅ Added cleaner streaming patterns using LambdaStreamingResponseHandler
- ✅ Created comprehensive Slidev presentation (40 slides with interactive demos)
- ✅ Added Jackson dependency for JSON parsing with JsonNode
- ✅ Created ResponsesApiDemo (Gson) and ResponsesApiJacksonDemo (Jackson)
- ✅ Fixed Responses API to use 'input' parameter instead of 'messages'
- ✅ Added JSON parsing comparison slides (Gson vs Jackson, JSON Pointer)
- ✅ Fixed slide layout issues including vertical RAG diagram
- ✅ Exported slides to PDF and PPTX formats for training materials
- ✅ Fixed critical bugs (file extension .png→.mp3 in TextToSpeechService)
- ✅ Applied consistent code formatting with Spotless
- ✅ Removed duplicate test methods

### Key Technologies & Versions
- **Java**: 21 (configured via Gradle toolchain, uses records, sealed interfaces, pattern matching)
- **LangChain4j**: 1.4.0 (primary AI framework with streaming enhancements)
- **Gradle**: 8.4+ with Kotlin DSL
- **Slidev**: For interactive presentations
- **JSON Parsing**:
  - Gson 2.13.1 (primary, used throughout)
  - Jackson 2.18.2 (for JsonNode and JSON Pointer support)
- **AI Models**:
  - OpenAI: gpt-4o-mini, gpt-5-nano (Responses API), dall-e-3, tts-1, whisper-1
  - Ollama: gemma3 (text), moondream (vision)
  - Google: gemini-2.0-flash-exp

### Project Structure
```
src/main/java/com/kousenit/
├── demos/                     # Standalone demo classes
│   ├── QuickChatDemo.java    # Basic chat example
│   ├── TextToSpeechDemo.java # TTS demonstration
│   ├── MultiModelDemo.java   # Compare multiple providers
│   ├── StreamingDemo.java    # Streaming with LambdaStreamingResponseHandler
│   ├── ResponsesApiDemo.java # Responses API with Gson parsing
│   └── ResponsesApiJacksonDemo.java # Responses API with Jackson
├── DalleService.java          # DALL-E image generation
├── EasyRAGDemo.java           # RAG implementation
├── OllamaService.java         # Ollama integration
├── OpenAiService.java         # OpenAI API wrapper
├── TextToSpeechService.java   # TTS implementation
├── Utils.java                 # Utility functions
├── TestCategories.java        # JUnit test tags
├── OllamaRecords.java         # Ollama data models
└── OpenAiRecords.java         # OpenAI data models

Training Materials:
├── slides.md                  # Slidev source (38 interactive slides)
├── slides.pdf                 # PDF export (775 KB)
└── slides.pptx                # PowerPoint export (28 MB)
```

## Development Guidelines

### Code Quality Standards
- **Formatting**: Spotless with Palantir Java Format (auto-applied)
- **Testing**: JUnit 5 with integration tests for external services
- **Error Handling**: Currently uses RuntimeException wrapping (appropriate for teaching)
- **Documentation**: Comprehensive labs.md with step-by-step instructions

### External Dependencies
- **Required**: OpenAI API key (OPENAI_API_KEY environment variable)
- **Optional**: Ollama installation for local AI models
- **Testing**: Some tests require external services to be available

### Build Commands
```bash
./gradlew build          # Full build
./gradlew compileJava    # Compile only (faster for dependency changes)
./gradlew test           # Run tests (some require external services)
./gradlew spotlessApply  # Apply code formatting
```

## Architecture Patterns

### Service Layer
Each AI provider has a dedicated service class:
- **TextToSpeechService**: OpenAI TTS API wrapper
- **OpenAiService**: General OpenAI API operations
- **OllamaService**: Local Ollama model interactions
- **DalleService**: Image generation with DALL-E

### Data Models
Uses Java records for API data modeling:
- **Sealed interfaces**: `OllamaRequest` with permitted implementations
- **Pattern matching**: Used for request type discrimination
- **JSON mapping**: Gson with field naming policies

### HTTP Client Usage
- Static HttpClient instances for connection pooling and reuse
- Avoided try-with-resources pattern (creates new instances inefficiently)
- Direct exception wrapping in RuntimeException
- Java 21's AutoCloseable HttpClient support acknowledged but not utilized for efficiency

## Common Tasks

### Adding New AI Models
1. Update model constants in service classes
2. Update test assertions that check available models
3. Update documentation in labs.md and README.md

### Dependency Updates
1. Check for latest versions in build.gradle.kts
2. Test compilation and basic functionality
3. Run spotlessApply for formatting
4. Update version numbers in documentation

### Bug Fixes
- **Priority**: Fix critical bugs that break core functionality
- **Testing**: Ensure changes don't break existing tests
- **Documentation**: Update labs.md if changes affect exercises

## Testing Strategy

### Test Categories
- **Unit Tests**: Fast tests with minimal external dependencies
- **Integration Tests**: Tests requiring external services (OpenAI, Ollama)
- **Test Tags** (via TestCategories interface):
  - `@Tag("local")`: No external services required
  - `@Tag("cheap")`: Minimal API cost
  - `@Tag("demo")`: Suitable for live demonstrations
  - `@Tag("expensive")`: Higher API costs
- **Custom Gradle Tasks**:
  - `./gradlew localTests`: Run only local tests
  - `./gradlew cheapTests`: Run low-cost API tests
  - `./gradlew demoTests`: Run demonstration tests

### Known Test Issues
- Some tests fail when external services are unavailable
- Tests use real API calls (educational approach, not production-ready)
- Rate limiting may affect test execution

## Educational Focus

### Learning Objectives
1. **AI API Integration**: Practical patterns for various AI services
2. **Modern Java**: Records, sealed interfaces, pattern matching
3. **Error Handling**: Real-world API error scenarios
4. **Build Tools**: Gradle with modern plugins and dependencies

### Teaching Approach
- **Hands-on Labs**: Step-by-step exercises in labs.md
- **Working Examples**: Complete implementations as reference
- **Progressive Complexity**: From simple API calls to complex RAG systems

## Maintenance Notes

### Code Review Findings
The codebase was reviewed and major issues addressed:
- ✅ Fixed file extension bug (critical)
- ✅ Removed duplicate tests
- 📝 Other issues noted but left unfixed for educational purposes

### Future Considerations
- **Security**: API key handling could be improved for production use
- **Error Handling**: Could implement custom exception hierarchy
- **Testing**: Could add mock testing for better unit test isolation
- **Documentation**: Could add JavaDoc for public APIs

## Contributing Guidelines

This is primarily an educational repository. Changes should:
1. **Maintain Educational Value**: Keep examples clear and understandable
2. **Follow Existing Patterns**: Consistency with current code style
3. **Update Documentation**: Ensure labs.md reflects any changes
4. **Test Thoroughly**: Verify examples work as documented

## Recent Architectural Decisions

### Java 21 Adoption
- Configured via Gradle toolchain for consistent builds
- Leverages data-oriented programming features
- Pattern matching for switch expressions
- Enhanced sealed interfaces support

### Streaming Improvements
- Migrated to LambdaStreamingResponseHandler utilities
- Cleaner lambda-based streaming patterns
- Removed verbose anonymous inner classes
- Examples: `onPartialResponse`, `onPartialResponseAndError`

### JSON Parsing Strategy
- **Dual approach**: Gson (primary) and Jackson (alternative)
- **Gson**: Used throughout existing services, JsonElement tree navigation
- **Jackson**: Added for JsonNode with JSON Pointer support (`at("/path/to/element")`)
- **Rationale**: Shows how to work with new APIs before framework support
- **Demo classes**: ResponsesApiDemo (Gson) vs ResponsesApiJacksonDemo (Jackson)

### Demo-First Approach
- Created standalone demo classes separate from tests
- Enables live coding during training sessions
- Reduces dependency on test framework during presentations
- Clear separation of concerns

### Training Materials
- Comprehensive Slidev presentation system (40 slides)
- Interactive demos with progressive reveals
- Export capabilities for multiple formats (PDF, PPTX)
- Mermaid diagrams for architecture visualization
- JSON parsing comparison slides added

## Recent Commit History Context

- **Model Updates**: Systematic update of AI models to latest versions
- **LangChain4j 1.4.0**: Major version upgrade with streaming enhancements
- **Java 21 Configuration**: Toolchain support for modern Java features
- **JSON Parsing**: Added Jackson alongside Gson for dual approach demonstration
- **Responses API**: Created demos showing new OpenAI API with both parsers
- **Training Materials**: Created comprehensive slide deck (40 slides) with exports
- **Slide Enhancements**: Added JSON parsing comparison and fixed layout issues
- **Code Organization**: Separated demos from tests for better presentation flow
- **Bug Fixes**: Addressed critical issues including Responses API parameter fix
- **Documentation**: Enhanced README, CLAUDE.md, and added "Going Further" section to labs

This project demonstrates practical AI integration patterns while maintaining educational clarity and hands-on learning opportunities. The recent additions emphasize understanding fundamentals (HTTP + JSON) to work with cutting-edge APIs before framework support exists.