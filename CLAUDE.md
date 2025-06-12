# AI Java Labs - Development Context

This file provides context for AI assistants working on the AI Java Labs project.

## Project Overview

**AI Java Labs** is an educational Java project demonstrating integration with various AI services and tools. It serves as a teaching repository with hands-on exercises for learning AI API integration patterns in Java.

## Current State

### Recent Updates (December 2024)
- ✅ Updated all AI models to latest versions (gemma2→gemma3, GPT models→gpt-4.1-nano)
- ✅ Updated dependencies to latest versions (Spotless 7.0.4, JUnit 5.13.0, etc.)
- ✅ Fixed critical bugs (file extension .png→.mp3 in TextToSpeechService)
- ✅ Added comprehensive table of contents to labs.md
- ✅ Applied consistent code formatting with Spotless
- ✅ Removed duplicate test methods

### Key Technologies & Versions
- **Java**: 17+ (uses modern features like records, sealed interfaces, pattern matching)
- **LangChain4j**: 1.0.1 (primary AI framework)
- **Gradle**: 8.4+ with Kotlin DSL
- **AI Models**:
  - OpenAI: gpt-4.1-nano, dall-e-3, tts-1, whisper-1
  - Ollama: gemma3 (text), moondream (vision)

### Project Structure
```
src/main/java/com/kousenit/
├── DalleService.java          # DALL-E image generation
├── EasyRAGDemo.java           # RAG implementation
├── OllamaService.java         # Ollama integration
├── OpenAiService.java         # OpenAI API wrapper
├── TextToSpeechService.java   # TTS implementation
├── Utils.java                 # Utility functions
├── OllamaRecords.java         # Ollama data models
└── OpenAiRecords.java         # OpenAI data models
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
- Static HttpClient instances (educational simplicity)
- Try-with-resources for AutoCloseable clients (Java 21+)
- Direct exception wrapping in RuntimeException

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
- **Mixed Approach**: Current tests are primarily integration-focused

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

## Recent Commit History Context

- **Model Updates**: Systematic update of AI models to latest versions
- **Dependency Management**: Regular updates to latest stable versions
- **Bug Fixes**: Addressed critical issues found in code review
- **Documentation**: Enhanced README and added comprehensive lab instructions

This project demonstrates practical AI integration patterns while maintaining educational clarity and hands-on learning opportunities.