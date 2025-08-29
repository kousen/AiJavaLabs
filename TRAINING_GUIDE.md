# AI Java Labs - Training Guide

## Test Organization for Cost Management

All tests are categorized with JUnit 5 tags to control execution and costs during training.

### Test Categories

| Tag | Description | Cost | Use Case |
|-----|-------------|------|----------|
| `@Tag("local")` | Tests using local Ollama | FREE | Safe for unlimited demos |
| `@Tag("cheap")` | Tests using low-cost models | $ | Good for frequent demos |
| `@Tag("expensive")` | Tests using costly models (DALL-E) | $$$ | Use sparingly |
| `@Tag("demo")` | Quick tests for live demos | Varies | Optimized for presentations |
| `@Tag("integration")` | Tests calling external APIs | Varies | All non-local tests |
| `@Tag("openai")` | Tests requiring OpenAI API | $ | OpenAI-specific features |

### Running Specific Test Categories

```bash
# Run only free local tests
./gradlew testLocal

# Run only cheap API tests
./gradlew testCheap

# Run tests suitable for demos
./gradlew testDemo

# Run all tests EXCEPT expensive ones
./gradlew testNotExpensive

# Run only OpenAI tests
./gradlew testOpenAI

# Run all tests (including expensive)
./gradlew test
```

## Demo Classes for Live Presentations

Located in `src/main/java/com/kousenit/demos/`:

### 1. QuickChatDemo
- **Purpose**: Basic chat interaction with OpenAI
- **Cost**: Low (gpt-5-nano)
- **Runtime**: ~1-2 seconds
- **Good for**: Opening demonstrations

### 2. TextToSpeechDemo
- **Purpose**: Generate MP3 from text
- **Cost**: Low (gpt-4o-mini-tts)
- **Runtime**: ~2-3 seconds
- **Good for**: Showing multimodal capabilities

### 3. LocalOllamaDemo
- **Purpose**: Demonstrate local AI models
- **Cost**: FREE
- **Runtime**: ~3-5 seconds
- **Good for**: Showing cost-free alternative

### 4. MultiModelDemo
- **Purpose**: Compare multiple AI providers
- **Cost**: Low
- **Runtime**: ~5-10 seconds
- **Good for**: Showing LangChain4j abstraction

### 5. EasyRAGDemo (existing)
- **Purpose**: RAG with document search
- **Cost**: Low per query
- **Runtime**: Interactive
- **Good for**: Advanced features

### 6. ResponsesApiDemo
- **Purpose**: Show new OpenAI Responses API (raw HTTP)
- **Cost**: Low (gpt-5-nano)
- **Runtime**: ~2-3 seconds
- **Good for**: Teaching why HTTP/JSON skills matter

### 7. ApiComparisonDemo
- **Purpose**: Compare Framework vs Raw HTTP approaches
- **Cost**: Low (3 quick calls)
- **Runtime**: ~5 seconds
- **Good for**: Demonstrating framework trade-offs

### 8. StreamingDemo
- **Purpose**: Show clean streaming with LambdaStreamingResponseHandler
- **Cost**: Low (gpt-4o-mini)
- **Runtime**: ~3-5 seconds
- **Good for**: Demonstrating modern functional streaming patterns

## Running Demos

```bash
# Run a specific demo
./gradlew run -PmainClass=com.kousenit.demos.QuickChatDemo

# Or compile and run directly
./gradlew compileJava
java -cp build/classes/java/main:build/libs/* com.kousenit.demos.QuickChatDemo
```

## Cost-Saving Tips for Training

1. **Start with Local**: Use Ollama demos first (free)
2. **Use Test Categories**: Run `testDemo` for quick, cheap tests
3. **Avoid DALL-E**: Image generation is expensive (~$0.04/image)
4. **Cache Responses**: Consider recording demo outputs for replay
5. **Use Cheap Models**: 
   - gpt-5-nano for text
   - gpt-4o-mini-tts for speech
   - gemini-2.0-flash-exp (Google's free tier)

## Environment Variables Required

```bash
export OPENAI_API_KEY="your-key-here"
export GOOGLEAI_API_KEY="your-key-here"  # Optional
export PERPLEXITY_API_KEY="your-key-here"  # Optional
```

## Pre-Training Checklist

- [ ] Install Ollama locally: `https://ollama.ai`
- [ ] Pull Ollama models: `ollama pull gemma3`
- [ ] Set environment variables
- [ ] Run `./gradlew testLocal` to verify setup
- [ ] Run `./gradlew testDemo` to test API connections
- [ ] Review demo classes and pick favorites
- [ ] Consider running expensive tests once and saving outputs

## Model Costs (Approximate)

| Model | Input Cost | Output Cost | Notes |
|-------|------------|-------------|-------|
| gpt-5-nano | $0.00015/1K tokens | $0.0006/1K tokens | Cheapest OpenAI |
| gpt-4o-mini-tts | ~$0.006/minute | N/A | Fast TTS |
| dall-e-3 | $0.04/image | N/A | Expensive! |
| gemma3 (Ollama) | FREE | FREE | Local model |
| gemini-2.0-flash | FREE (limited) | FREE (limited) | Google's free tier |

## Presentation Flow Suggestion

1. Start with concept explanation
2. Show `QuickChatDemo` - basic interaction
3. Show `LocalOllamaDemo` - free alternative
4. Show `MultiModelDemo` - provider comparison
5. Show `TextToSpeechDemo` - multimodal
6. **Framework vs Raw API Discussion**:
   - Run `ApiComparisonDemo` - show all three approaches
   - Discuss trade-offs (convenience vs flexibility)
   - Show `ResponsesApiDemo` - new API without framework support
   - Key lesson: "Frameworks lag behind new APIs"
7. Run `./gradlew testDemo` - show test organization
8. Deep dive into specific features as needed

## Key Teaching Point: Why Learn Both?

Use the Responses API demos to illustrate:
- **Frameworks are great** for common tasks and abstraction
- **Raw HTTP/JSON skills** let you adopt new APIs immediately
- **Real-world example**: Responses API launched March 2025, still no LC4j support (Aug 2025)
- **Lesson**: Don't be blocked by framework limitations - know the fundamentals!