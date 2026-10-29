# AI Java Labs - Development Context

This file provides context for AI assistants working on the AI Java Labs project.

## Project Overview

**AI Java Labs** is an educational Java project demonstrating integration with various AI services and tools. It serves as a teaching repository with hands-on exercises for learning AI API integration patterns in Java.

## Current State

### Recent Updates (October 2026)
- ✅ Upgraded LangChain4j to 1.23.0
- ✅ Routed QuickChatDemo and MultiModelDemo through OpenRouter (one key, provider-prefixed model ids)
- ✅ Added DecisionModelDemo using the experimental `DecisionModel` API (TypeSafe Jev via OpenRouter)
- ✅ Removed `temperature(0.0)` from gpt-5-nano demos and labs (reasoning models reject non-default values)
- ✅ Switched StreamingDemo to the `*Blocking` handler variants so main waits for the stream
- ✅ Compile with `-parameters` so `@Tool` argument names reach the model
- ✅ Replaced Gson with Jackson 3 (`tools.jackson`) throughout; merged the two Responses API demos into one
- ✅ Added HttpSupport.requireSuccess and ApiException; every raw HTTP service now checks status codes, with a lab step that provokes a real 401
- ✅ Moved image examples to gpt-image-2.5-flare (gpt-image-2 still served, but 2.5 is what OpenAI now documents)

### Earlier Updates (May 2026)
- ✅ Updated all AI models to latest versions (local models→gpt-oss, GPT models→gpt-5-nano)
- ✅ Updated GPT Image examples from deprecated DALL-E 3 to gpt-image-2
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
- **LangChain4j**: 1.23.0 (primary AI framework; includes the experimental `DecisionModel` API)
- **Gradle**: 8.4+ with Kotlin DSL
- **Slidev**: For interactive presentations
- **JSON Parsing**:
  - Jackson 3.2.4 (`tools.jackson.core:jackson-databind`); Gson removed in October 2026
  - LangChain4j pulls in Jackson 2 (`com.fasterxml.jackson`) transitively; both coexist, annotations are shared
- **AI Models**:
  - OpenAI: gpt-4o-mini, gpt-5-nano (Responses API), gpt-image-2.5-flare (gpt-image-2.5-sunburst for edits), gpt-4o-mini-tts, whisper-1
  - Ollama: gpt-oss (text), moondream (vision)
  - Google: gemini-3-flash-preview
  - OpenRouter (QuickChatDemo, MultiModelDemo): openai/gpt-5-nano, google/gemini-3-flash-preview via `OpenAiChatModel` with base URL `https://openrouter.ai/api/v1`

### Project Structure
```
src/main/java/com/kousenit/
├── demos/                     # Standalone demo classes
│   ├── QuickChatDemo.java    # Basic chat example
│   ├── TextToSpeechDemo.java # TTS demonstration
│   ├── MultiModelDemo.java   # Compare multiple providers
│   ├── ToolCallingDemo.java  # LangChain4j tools backed by Java methods
│   ├── StructuredOutputDemo.java # Java records from model output
│   ├── StreamingDemo.java    # Streaming with LambdaStreamingResponseHandler
│   ├── DecisionModelDemo.java # Experimental DecisionModel API: typed answers with probabilities
│   └── ResponsesApiDemo.java # Responses API with Jackson's tree model (pointer + walk)
├── GptImageService.java       # GPT Image generation
├── EasyRAGDemo.java           # RAG implementation
├── OllamaService.java         # Ollama integration
├── OpenAiService.java         # OpenAI API wrapper
├── TextToSpeechService.java   # TTS implementation
├── Utils.java                 # Utility functions
├── TestCategories.java        # JUnit test tags
├── OllamaRecords.java         # Ollama data models
└── OpenAiRecords.java         # OpenAI data models

Training Materials:
└── slides.md                  # Slidev source; CI publishes the PDF release and GitHub Pages deck
```

## Development Guidelines

### Code Quality Standards
- **Formatting**: Spotless with Palantir Java Format (auto-applied)
- **Testing**: JUnit 5 with integration tests for external services
- **Error Handling**: Currently uses RuntimeException wrapping (appropriate for teaching)
- **Documentation**: Comprehensive labs.md with step-by-step instructions

### External Dependencies
- **Required**: OpenAI API key (OPENAI_API_KEY environment variable)
- **Required for QuickChatDemo/MultiModelDemo**: OpenRouter API key (OPENROUTER_API_KEY environment variable)
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
- **GptImageService**: Image generation with GPT Image

### Data Models
Uses Java records for API data modeling:
- **Sealed interfaces**: `OllamaRequest` with permitted implementations
- **Pattern matching**: Used for request type discrimination
- **JSON mapping**: Jackson 3 with the SNAKE_CASE naming strategy

### HTTP Client Usage
- Static HttpClient instances for connection pooling and reuse
- Avoided try-with-resources pattern (creates new instances inefficiently)
- Direct exception wrapping in RuntimeException
- `HttpSupport.requireSuccess` checks the status code on every raw call and throws `ApiException` (status + provider message + Retry-After); `TextToSpeechService` handles the `ofFile` case itself because the error body lands in the audio file
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
- **Library**: Jackson 3 (`tools.jackson.databind`). Mappers are immutable: `JsonMapper.builder().propertyNamingStrategy(SNAKE_CASE).build()`. Exceptions are unchecked (`JacksonException`). `FAIL_ON_UNKNOWN_PROPERTIES` is off by default in 3.x.
- **Why Jackson 3**: it is what Spring Boot 4 ships. Gson's README describes it as in maintenance mode. JEP 540 (Simple JSON API, incubator) is targeted at JDK 28 and is mentioned in the labs and slides, not used.
- **Coexistence**: LangChain4j still depends on Jackson 2 (`com.fasterxml.jackson`). Different packages, shared `com.fasterxml.jackson.annotation` annotations, so both sit on the classpath. Course code imports `tools.jackson.*` only; `@JsonProperty` comes from the shared annotations package.
- **Teaching split**: records (data binding) when the shape is known, `JsonNode` with `at()`/`path()` when it varies. ResponsesApiDemo shows both; its output array proves why `/output/0/...` is the wrong pointer for reasoning models.
- **Request bodies**: records serialized with `writeValueAsString`; services whose records have nullable fields (GptImageService, LMStudioService) set NON_NULL inclusion.

### Tool Calling Strategy
- **High-level approach**: LangChain4j AI Services with `@Tool` annotated Java methods
- **Demo class**: ToolCallingDemo
- **Teaching point**: The model chooses when to call a tool, but Java owns validation, execution, side effects, and logging
- **Current example**: Travel assistant using weather, driving distance, and fuel-cost tools

### Structured Output Strategy
- **High-level approach**: LangChain4j AI Services returning Java records with JSON Schema support enabled
- **Demo class**: StructuredOutputDemo
- **Teaching point**: Structured outputs turn model interpretation into application data, but Java still validates before use
- **Current example**: CourseInquiry record with priority, follow-up flag, and action items
- **Model constraint**: gpt-5-nano is a reasoning model and rejects `temperature` values other than the default; do not set it in demos or labs

### Decision Model Strategy
- **High-level approach**: LangChain4j's experimental `DecisionModel` API (`langchain4j-typesafe` module) with `TypeSafeDecisionModel` pointed at OpenRouter (base URL `https://openrouter.ai/api`, model `typesafe/jev-1.13`)
- **Demo class**: DecisionModelDemo
- **Teaching point**: Structured output gives one answer; a decision model gives a probability for every option, so thresholds and escalation rules live in Java
- **Current example**: Triage of training inquiries with a choice question (course), a yes/no question (asks for a next step), and a scale question (urgency)
- **Caveat**: The API is marked experimental; re-verify method names when upgrading LangChain4j

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
- Tool calling lab and demo added with LangChain4j `@Tool` annotations
- Structured output lab and demo added with LangChain4j JSON Schema support
- OpenRouter lab added before the tool-calling lab; decision model "Going Further" section and slide added

## Recent Commit History Context

- **Model Updates**: Systematic update of AI models to latest versions
- **LangChain4j 1.23.0**: Current pinned framework version for the course
- **Java 21 Configuration**: Toolchain support for modern Java features
- **JSON Parsing**: Added Jackson alongside Gson for dual approach demonstration
- **Responses API**: Created demos showing new OpenAI API with both parsers
- **Tool Calling**: Added runnable LangChain4j demo showing Java methods as model-callable tools
- **Structured Output**: Added runnable LangChain4j demo showing model extraction into Java records
- **Training Materials**: Created comprehensive slide deck (40 slides) with exports
- **Slide Enhancements**: Added JSON parsing comparison and fixed layout issues
- **Code Organization**: Separated demos from tests for better presentation flow
- **Bug Fixes**: Addressed critical issues including Responses API parameter fix
- **Documentation**: Enhanced README, CLAUDE.md, and added "Going Further" section to labs

This project demonstrates practical AI integration patterns while maintaining educational clarity and hands-on learning opportunities. The recent additions emphasize understanding fundamentals (HTTP + JSON) so developers can inspect, debug, and extend framework-based integrations when API behavior changes.
