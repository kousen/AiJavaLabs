# Labs

## Table of Contents

- [Generate Audio from Text](#generate-audio-from-text)
- [List the OpenAI Models](#list-the-openai-models)
- [Same pattern, other vendors](#same-pattern-other-vendors)
- [Install Ollama](#install-ollama)
- [Access Ollama programmatically](#access-ollama-programmatically)
- [Streaming Results](#streaming-results)
- [Fix the camel case issue](#fix-the-camel-case-issue)
- [Create a vision request](#create-a-vision-request)
- [Refactor to sealed interfaces](#refactor-to-sealed-interfaces)
- [Have a conversation](#have-a-conversation)
- [Call cloud models through OpenRouter](#call-cloud-models-through-openrouter)
- [Add tool calling with LangChain4j](#add-tool-calling-with-langchain4j)
- [Extract structured output into Java records](#extract-structured-output-into-java-records)
- [Generating Images](#generating-images)

## Generate Audio from Text

- At the [OpenAI developer site](https://platform.openai.com/docs/overview), sign up for an account and create an API key. Currently the link to your API keys is at https://platform.openai.com/api-keys . OpenAI now recommends project API keys. See [this page](https://help.openai.com/en/articles/9186755-managing-your-work-in-the-api-platform-with-projects) for more information.
- Add the API key to an environment variable called `OPENAI_API_KEY`.
- Create a new Java project called `aijavalabs` in your favorite IDE with either Gradle or Maven as your build tool.
- Review the documentation for the Open AI [Text-to-speech](https://platform.openai.com/docs/guides/text-to-speech) API.
- Create a new class called `TextToSpeechService` in a package of your choosing.
- Add a private, static, final attribute called `OPENAI_API_KEY` of type `String` that reads the API key from the environment variable.

```java
private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
```
- Add a method called `generateMp3` that returns a `java.nio.file.Path`. The method should take three arguments:
  - `model` of type `String`
  - `input` of type `String`
  - `voice` of type `String`

- Add a local variable called `payload`, which is a JSON string that contains the text you want to convert to speech:

```java
String payload = """
    {
        "model": "%s",
        "input": "%s",
        "voice": "%s"
    }
    """.formatted(model, input.replaceAll("\\s+", " ").trim(), voice);
```

- For those parameters:
  - The value of the `model` variable must be `tts-1`, `tts-1-hd`, or the newest one, `gpt-4o-mini-tts`. Use whichever one you prefer.
  - The value of `voice` must be one of: `alloy`, `ash`, `ballad`, `coral`, `echo`, `fable`, `nova`, `onyx`, `sage`, or `shimmer`. Pick any one you like.
  - The `input` parameter is the text you wish to convert to an mp3.

- Next, create an `HttpRequest` object that will include the `payload` in a POST request:

```java
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.openai.com/v1/audio/speech"))
    .header("Authorization", "Bearer %s".formatted(OPENAI_API_KEY))
    .header("Content-Type", "application/json")
    .header("Accept", "audio/mpeg")
    .POST(HttpRequest.BodyPublishers.ofString(payload))
    .build();
```

- To send a request, you need an `HttpClient` instance. While Java 21 made `HttpClient` implement `AutoCloseable`, it's more efficient to reuse a single instance. The `HttpClient` is thread-safe and designed to handle multiple concurrent requests through connection pooling. Create it as a static field in your class:

```java
public class TextToSpeechService {
    // Reuse a single HttpClient instance for better performance
    private static final HttpClient client = HttpClient.newHttpClient();

    public Path generateMp3(String model, String input, String voice) {
        // ... from before ...

        try {
            HttpResponse<Path> response =
                client.send(request, HttpResponse.BodyHandlers.ofFile(getFilePath()));
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

- You'll likely want to save the generated audio file into a unique file. The argument to `ofFile` is a `Path` object that represents the file where the response will be saved. Add the following private method to generate one:

```java
private Path getFilePath() {
    String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    String fileName = String.format("audio_%s.mp3", timestamp);
    Path dir = Paths.get("src/main/resources");
    return dir.resolve(fileName);
}
```

- Add a test to drive the system:

```java

@Test
void testGenerateMp3() {
  var service = new TextToSpeechService();
  Path result = service.generateMp3(
          "tts-1",
          """
          Now that I know how to generate an audio file,
          I can use it to add the ability to convert
          text to speech to any existing Java system.
          """,
          "alloy"
  );

  assertNotNull(result);
  System.out.println("Generated audio file: " + result.toAbsolutePath());
}
```

- Run the test. The console should show the name of an mp3 file saved in the `src/main/resources` folder. You can play the file to hear the text you provided.

- Now break it on purpose: change the `Authorization` header to use a bad key and run the test again. It still "passes" and still produces an mp3 file, but the file will not play. Open it in a text editor and you will find JSON: `{"error": {"message": "Incorrect API key provided: ..."}}`. The `ofFile` body handler saves whatever the server sends, and HTTP status codes are not exceptions. Any REST client has to check them. Add the check right after `send`:

```java
HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(filePath));
if (response.statusCode() / 100 != 2) {
    String body = Files.readString(filePath);   // the "audio" file holds the error JSON
    Files.deleteIfExists(filePath);
    throw new IllegalStateException("HTTP %d: %s".formatted(response.statusCode(), body));
}
return response.body();
```

- Run the test with the bad key one more time to see the failure message, then restore the real key. The next lab turns this check into a reusable helper.

## List the OpenAI Models

- Whenever you access the OpenAI API, you need to specify a model. To know which models are available, the OpenAI API provides an endpoint to list them. Review the documentation for that endpoint [here](https://platform.openai.com/docs/api-reference/models).
- You'll find that our task is to send an HTTP GET request to `https://api.openai.com/v1/models` with the required `Authorization` header, and that the result is a block of JSON data.
- We're going to want to parse that JSON data, as well as generate JSON for other interactions. For that, we need a JSON library. The JDK does not ship one yet: [JEP 540](https://openjdk.org/jeps/540) proposes a Simple JSON API as an incubator module, currently targeted at JDK 28. Until then, we'll use Jackson, the library Spring Boot uses. We use the Jackson 3 line, which lives in the `tools.jackson` package; Jackson 2 lives in `com.fasterxml.jackson`, and you will meet both in the wild. (Gson, the other common choice, describes itself as in maintenance mode: bugs are fixed, but large new features are not planned.) Add the following dependency to your build file:

```kotlin
dependencies {
    implementation("tools.jackson.core:jackson-databind:3.2.4")
}
```

- Add a class called `OpenAiRecords` to your project. It will hold all the records we need to parse the JSON data.
- Inside the `OpenAiRecords` class, add a record called `ModelList`, which itself contains a record called `Model`:

```java
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OpenAiRecords {
  // List the models
  public record ModelList(List<Model> data) {
    public record Model(
            String id,
            long created,
            @JsonProperty("owned_by") String ownedBy) {
    }
  }
}
```

- The annotation maps the JSON name `owned_by` to the record component `ownedBy`. Note the annotation package: Jackson 3 kept its annotations in `com.fasterxml.jackson.annotation`, so that import does not change between Jackson 2 and 3. Jackson ignores JSON fields that have no matching component (the response has more fields than we modeled), so records only need the fields you care about.

- Create a new class called `OpenAiService` in a package of your choosing.
- Add a private, static, final attribute called `OPENAI_API_KEY` of type `String` that reads the API key from the environment variable.

```java
private static final String API_KEY = System.getenv("OPENAI_API_KEY");
```

- Also add a constant for the Models endpoint:

```java
private static final String MODELS_URL = "https://api.openai.com/v1/models";
```

- Add a private, static, final `HttpClient` field that will be reused for all requests:

```java
// HttpClient is thread-safe and designed to be reused for better performance
private static final HttpClient client = HttpClient.newHttpClient();
```

- Add a private, final attribute called `mapper` of type `ObjectMapper`. Jackson 3 mappers are immutable, so all configuration goes through a builder:

```java
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

private final ObjectMapper mapper = JsonMapper.builder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build();
```

- The naming strategy converts every `snake_case` JSON name to a `camelCase` component name, which makes the `@JsonProperty` annotation above redundant. Keep it once so you have seen the explicit form; from here on we rely on the strategy.

- Add a method called `listModels` that returns a `ModelList`. This method will send a GET request to the OpenAI API:

```java
public ModelList listModels() {
    HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(MODELS_URL))
          .header("Authorization", "Bearer %s".formatted(API_KEY))
          .header("Accept", "application/json")
          .build();
    try {
        HttpResponse<String> response =
            client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(response.body(), ModelList.class);
    } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```

- Only the HTTP call needs the `try`/`catch`: Jackson 3's exceptions are unchecked (`JacksonException`), so `readValue` adds no checked exception of its own.

- Before writing the test, see what a failure looks like. Temporarily point `API_KEY` at a bad value and call `listModels`. Jackson throws a mapping error, because the body it was given is not a model list at all. It is OpenAI's error envelope, sent with a 401 status:

```json
{
  "error": {
    "message": "Incorrect API key provided: sk-bad. You can find your API key at https://platform.openai.com/account/api-keys.",
    "type": "invalid_request_error",
    "code": "invalid_api_key"
  }
}
```

- OpenAI, OpenRouter, and Anthropic all use this `{"error": {"message": ...}}` shape, and a 429 rate-limit response adds a `Retry-After` header saying how long to wait. Every service in this course needs the same check, so put it in a small shared class. `ApiException` carries the status so callers can tell a 429 from a 401:

```java
public class ApiException extends RuntimeException {
    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() { return status; }
    public boolean isRateLimited() { return status == 429; }
}
```

```java
public final class HttpSupport {
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    public static HttpResponse<String> requireSuccess(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status / 100 == 2) {
            return response;
        }
        String retryAfter = response.headers()
                .firstValue("Retry-After")
                .map(seconds -> " (Retry-After: %ss)".formatted(seconds))
                .orElse("");
        throw new ApiException(status, "HTTP %d from %s: %s%s".formatted(
                status, response.uri(), errorMessage(response.body()), retryAfter));
    }

    // The message inside the envelope, or the raw body when there is none (Ollama sends {"error": "..."})
    static String errorMessage(String body) {
        try {
            JsonNode message = MAPPER.readTree(body).at("/error/message");
            return message.isMissingNode() ? body : message.asString();
        } catch (JacksonException e) {
            return body;   // not JSON at all, for example an HTML page from a proxy
        }
    }
}
```

- The `at("/error/message")` call is Jackson's tree model, which we come back to later. For now, note that it returns a "missing node" rather than throwing when the path is absent. Wrap the `send` call in `listModels`:

```java
HttpResponse<String> response =
    requireSuccess(client.send(request, HttpResponse.BodyHandlers.ofString()));
return mapper.readValue(response.body(), ModelList.class);
```

- With the bad key, the failure now reads `HTTP 401 from https://api.openai.com/v1/models: Incorrect API key provided: sk-bad ...`. Restore the key. Use `requireSuccess` on every `send` from here on. The solution's `TextToSpeechService` uses the same helper for the file case from the previous lab.

- Exercise, if time permits: write a retry loop around `send` that catches `ApiException`, checks `isRateLimited()`, sleeps for the `Retry-After` value, and gives up after a few attempts. Keep the loop out of the service method itself, so the happy path stays readable.

- Add a test to drive the system:

```java
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static com.kousenit.OpenAiRecords.ModelList;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAiServiceTest {
  private final OpenAiService service = new OpenAiService();

  @Test
  void listModels() {
    List<String> models = service.listModels().data().stream()
            .map(ModelList.Model::id)
            .sorted()
            .toList();

    models.forEach(System.out::println);
    assertTrue(new HashSet<>(models).containsAll(
            List.of("gpt-image-2.5-flare", "gpt-5-nano",
                    "gpt-4o-mini-tts", "whisper-1")));
    }
}
```

- Run the test. You should see a list of model names in the console, and the assertion should pass.

## Same pattern, other vendors

Everything in the last lab was: build a request, send it, check the status, bind the JSON to records. That pattern does not care who is on the other end. This lab applies it to three vendors with no Java SDK, or none worth the dependency. Only the details change: which header carries the key, and what the JSON looks like.

### OpenRouter: a catalog with no key required

[OpenRouter](https://openrouter.ai) routes requests to hundreds of models from many vendors behind one OpenAI-compatible API, which we use later with LangChain4j. Its model catalog is a public GET, and the JSON is richer than OpenAI's list: each model carries its input and output modalities and its prices per token.

* Review the [models endpoint](https://openrouter.ai/docs/guides/overview/models). Note the `output_modalities` query parameter: the default view lists text models, and `image`, `audio`, `video`, `decisions`, or `all` widen it.
* Add `OpenRouterRecords` with just the fields we care about. Jackson ignores the rest:

```java
public class OpenRouterRecords {
    public record ModelList(List<Model> data, int totalCount) {}

    public record Model(String id, String name, int contextLength, Architecture architecture, Pricing pricing) {}

    public record Architecture(List<String> inputModalities, List<String> outputModalities) {}

    // Prices arrive as strings in dollars per token, so convert once and keep the precision
    public record Pricing(String prompt, String completion) {
        private static final BigDecimal MILLION = BigDecimal.valueOf(1_000_000);

        public BigDecimal promptPerMillion() {
            return prompt == null ? BigDecimal.ZERO : new BigDecimal(prompt).multiply(MILLION).stripTrailingZeros();
        }
    }
}
```

* Records can carry behavior. `promptPerMillion` turns `"0.00000025"` into the `$0.25 per million tokens` figure the pricing pages quote, and it lives next to the data it describes.
* Add `OpenRouterService` with a `listModels(String outputModalities)` method. There is no key, so there is no `Authorization` header:

```java
public List<Model> listModels(String outputModalities) {
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://openrouter.ai/api/v1/models?output_modalities=" + outputModalities))
            .header("Accept", "application/json")
            .build();
    try {
        HttpResponse<String> response = requireSuccess(client.send(request, HttpResponse.BodyHandlers.ofString()));
        return mapper.readValue(response.body(), ModelList.class).data();
    } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```

* Test it twice: once with `"text"`, asserting the ids `openai/gpt-5-nano` and `google/gemini-3-flash-preview` are present, and once with `"image"`, printing each model's modalities and prompt price per million and asserting every result lists `image` among its output modalities.

### Anthropic: a different header, the same shape

Anthropic's API uses an `x-api-key` header instead of a bearer token, plus a required `anthropic-version` header. Its models endpoint returns `created_at` as an ISO-8601 timestamp, which Jackson 3 reads into a `java.time.Instant` with no extra configuration.

```java
public record ModelsResponse(List<Model> data, String firstId, boolean hasMore, String lastId) {}
public record Model(Instant createdAt, String displayName, String id, String type) {}
```

```java
HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.anthropic.com/v1/models"))
        .header("x-api-key", System.getenv("ANTHROPIC_API_KEY"))
        .header("anthropic-version", "2023-06-01")
        .header("Accept", "application/json")
        .build();
```

* If you have an Anthropic key, add `AnthropicService.listModels()` on this pattern and print the display names and creation dates. If not, read the solution's `AnthropicService` and note how little differs from `OpenAiService`.

### Optional: ElevenLabs, text to speech without an SDK

[ElevenLabs](https://elevenlabs.io) is a popular text-to-speech service with official SDKs for Python and JavaScript and none for Java. The API is two calls: a GET for the available voices and a POST that returns audio bytes, the same shape as the OpenAI text-to-speech lab. Skip this section if you do not want another account; it costs nothing to read.

* Create an account, generate a key, and set `ELEVENLABS_API_KEY`. Review the [text-to-speech endpoint](https://elevenlabs.io/docs/api-reference/text-to-speech/convert): the key travels in an `xi-api-key` header, the voice id is part of the URL, and the model id goes in the body.
* Add the records:

```java
public class ElevenLabsRecords {
    public record VoiceList(List<Voice> voices) {}
    public record Voice(String voiceId, String name, String category) {}
    public record SpeechRequest(String text, String modelId) {}
}
```

* Add `ElevenLabsService` with `listVoices()` (a GET to `/v1/voices` with the header) and a `textToSpeech` method. The audio handling is the text-to-speech lab again, with the status check from the helper:

```java
public Path textToSpeech(String voiceId, String text) {
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.elevenlabs.io/v1/text-to-speech/" + voiceId))
            .header("xi-api-key", API_KEY)
            .header("Content-Type", "application/json")
            .header("Accept", "audio/mpeg")
            .POST(HttpRequest.BodyPublishers.ofString(
                    mapper.writeValueAsString(new SpeechRequest(text, "eleven_multilingual_v2"))))
            .build();
    Path filePath = outputPath();
    try {
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(filePath));
        return HttpSupport.requireSuccessFile(response).body();
    } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```

* Every account includes ElevenLabs' premade voices (George, Sarah, Charlie, and others). Call `listVoices()` first, pick one by name, and pass its id. Never hardcode a voice id from someone else's account into a shared repository; cloned voices are private.
* Write a test that lists the voices, generates a sentence with a premade voice, checks the file is more than a few kilobytes, and deletes the file afterwards so generated audio stays out of version control.
* `GET /v1/models` lists the speech models if you want to try a newer one than the default.

Three vendors, three header schemes, one pattern. That is the whole argument for learning the HTTP layer: the next service you need will not have a Java library either, and it will not matter.

## Install Ollama

- Download the latest version of Ollama from the [Ollama website](https://ollama.com).
- Run the installer and follow the instructions.
- From a command prompt, enter `ollama run gemma4` to download and install Google's open-source Gemma 4 model locally. It's a small, capable multimodal model that handles both text and vision, so we'll use it throughout these labs.
- Try out a couple of sample prompts at the command line, like `Why is the sky blue?` or `Given the power required to train large language models, how do companies ever expect to make money?`.
- When you're finished, type `/bye` to exit.

## Access Ollama programmatically

- Ollama installs a small web server on your system. Verify that it is running by accessing [http://localhost:11434](http://localhost:11434) in a browser. You should get back the string, `Ollama is running`.
- Ollama maintains a programmatic API accessible through a RESTful web service. The documentation is located [here](https://github.com/ollama/ollama/blob/main/docs/api.md).
- We'll start by accessing the `api/generate` endpoint, as it is the simplest. It takes a JSON object with three fields: `model`, `prompt`, and `stream`. The `model` field is the name of the model you want to use (`gemma4` in our case), the `prompt` field is the question you want the model to answer, and the `stream` field is a boolean that determines whether the response should be streamed back to the client.
- Create a Java project (or simply add to the existing one) in your favorite IDE with either Gradle or Maven as your build tool.
- Create a class called `OllamaService` in a package of your choosing.
- Add a method called `generate` that takes a `String` and returns a `String`.
- If you created a new project, add the Jackson dependency to your build file and synchronize the project. If you added to an existing project, you can skip this step.
- Model the input and output JSON with Java records. To do so, add a class called `OllamaRecords` to your project.

```java
public class OllamaRecords {
    public record OllamaTextRequest(String model, String prompt, boolean stream) {
    }
}
```

- The output consists of a JSON object with `model`, `created_at`, `response`, and `done` field (among other optional fields we won't use right now). Add a record for that:

```java
public class OllamaRecords {
    public record OllamaTextRequest(
            String model,
            String prompt,
            boolean stream) {
    }

    public record OllamaResponse(
            String model,
            String createdAt,
            String response,
            boolean done) {
    }
}
```

- Now we have enough to create the `OllamaService` class and use it to access the chat endpoint. Add the following code to the `OllamaService` class:

```java
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import static com.kousenit.OllamaRecords.*;

public class OllamaService {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String URL = "http://localhost:11434";

    private final ObjectMapper mapper = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    public OllamaResponse generate(OllamaTextRequest ollamaRequest) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL + "/api/generate"))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(
                    mapper.writeValueAsString(ollamaRequest)))
            .build();
        try {
            HttpResponse<String> response =
                    requireSuccess(client.send(request, HttpResponse.BodyHandlers.ofString()));
            return mapper.readValue(response.body(), OllamaResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

* This time the request body is a record too: `writeValueAsString` turns `OllamaTextRequest` into JSON, so you never hand-build a JSON string the way the text-to-speech lab did.
* `requireSuccess` is the helper from the OpenAI lab. Ollama's errors have a different shape, `{"error": "model 'gemma5' not found"}` with a 404, so the helper falls back to printing the raw body. Try a model name you have not pulled to see it.

* Create a test class to verify the `OllamaService` class works as expected. Here is a sample:

```java
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static com.kousenit.OllamaRecords.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class OllamaServiceTest {
    private final OllamaService service = new OllamaService();

    @Test
    public void testGenerate() {
        var ollamaRequest = new OllamaTextRequest("gemma4", "Why is the sky blue?", false);
        OllamaResponse ollamaResponse = service.generate(ollamaRequest);
        String answer = ollamaResponse.response();
        System.out.println(answer);
        assertTrue(answer.contains("scattering"));
    }
}
```

* Run the test to verify the service works as expected.
* Add an overload of the `generate` method to `OllamaService` that takes two Strings: one for the `model` and one for the `prompt`, and returns an `OllamaResponse` object.

```java
public OllamaResponse generate(String model, String prompt) {
    return generate(new OllamaTextRequest(model, prompt, false));
}
```

* Adding convenience methods like that to make the calls easier for a client is a common practice. Add a test to verify the new method works.

```java
@Test
void generate_with_model_and_name() {
    var ollamaResponse = service.generate("gemma4", "Why is the sky blue?");
    String answer = ollamaResponse.response();
    System.out.println(answer);
    assertTrue(answer.contains("scattering"));
}
```

## Streaming Results

* To see what a streaming result looks like, add a method to send the message and return the response as a `String`:

```java
public String generateStreaming(OllamaTextRequest ollamaRequest) {
    HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(URL + "/api/generate"))
          .header("Content-Type", "application/json")
          .header("Accept", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(ollamaRequest)))
          .build();
  try {
    HttpResponse<String> response = requireSuccess(client.send(request, HttpResponse.BodyHandlers.ofString()));
    return response.body();
  } catch (IOException | InterruptedException e) {
    throw new RuntimeException(e);
  }
}
```

* Add a test to see the streaming result:

```java
@Test
public void streaming_generate_request() {
    var request = new OllamaTextRequest("gemma4", "Why is the sky blue?", true);
    String response = service.generateStreaming(request);
    System.out.println(response);
}
```

* Run the test to see the streaming result. It will be similar to:

```json lines
{"model":"gemma4","created_at":"2024-07-08T20:15:46.965689Z","response":"The","done":false}
{"model":"gemma4","created_at":"2024-07-08T20:15:46.991009Z","response":" sky","done":false}
{"model":"gemma4","created_at":"2024-07-08T20:15:47.019408Z","response":" appears","done":false}
{"model":"gemma4","created_at":"2024-07-08T20:15:47.050245Z","response":" blue","done":false}
{"model":"gemma4","created_at":"2024-07-08T20:15:47.095448Z","response":" due","done":false}
{"model":"gemma4","created_at":"2024-07-08T20:15:47.126318Z","response":" to","done":false}
```

followed by lots more lines, until the `done` field is `true`.

* That shows what a streaming response looks like, but waiting for the whole body and printing it at the end defeats the purpose. The JDK's `HttpClient` can hand you the body as it arrives: `sendAsync` returns a `CompletableFuture` immediately, and `BodyHandlers.ofLines()` delivers the body as a `Stream<String>` of lines while the response is still in flight. Each Ollama line is a complete JSON object, so each line maps to an `OllamaResponse`. Add this method, which takes a `Consumer<String>` to call for every token and returns the full text at the end:

```java
public String generateStreaming(String model, String prompt, Consumer<String> onToken) {
    var ollamaRequest = new OllamaTextRequest(model, prompt, true);
    var request = HttpRequest.newBuilder()
            .uri(URI.create(URL + "/api/generate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(ollamaRequest)))
            .build();

    CompletableFuture<String> fullText = client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
            .thenApply(HttpSupport::requireSuccessLines)
            .thenApply(response -> {
                var text = new StringBuilder();
                response.body()
                        .map(line -> mapper.readValue(line, OllamaResponse.class))
                        .forEach(chunk -> {
                            onToken.accept(chunk.response());
                            text.append(chunk.response());
                        });
                return text.toString();
            });

    return fullText.join();
}
```

* `requireSuccessLines` is the streaming twin of `requireSuccess`: a failed request still delivers its error body, just as lines. Add it to `HttpSupport`:

```java
public static HttpResponse<Stream<String>> requireSuccessLines(HttpResponse<Stream<String>> response) {
    int status = response.statusCode();
    if (status / 100 == 2) {
        return response;
    }
    String body = response.body().collect(Collectors.joining("\n"));
    throw new ApiException(status, "HTTP %d from %s: %s".formatted(status, response.uri(), errorMessage(body)));
}
```

* Add a test that collects the tokens and watches them print one at a time:

```java
@Test
void streaming_tokens_arrive_one_at_a_time() {
    List<String> tokens = new ArrayList<>();
    String full = service.generateStreaming("gemma4", "Count from 1 to 5, digits only.", token -> {
        System.out.print(token);
        tokens.add(token);
    });

    assertThat(tokens).hasSizeGreaterThan(1);
    assertThat(String.join("", tokens)).isEqualTo(full);
}
```

* The `join()` at the end blocks until the stream finishes, which is what a test or a `main` method wants. In a server you would return the `CompletableFuture` and let the caller decide. Either way, the streaming itself is done by the JDK: no library was needed.

* Cloud providers stream a little differently. OpenAI's chat endpoint uses server-sent events: each line starts with `data: `, the payload after the prefix is JSON, and the stream ends with `data: [DONE]`. The code is the same shape as above with one extra `filter` and a `substring` to strip the prefix. LangChain4j's streaming models do exactly this for you, which we see later in the course.

## Fix the camel case issue

* Print the full `OllamaResponse` in one of the tests and look at `createdAt`: it is `null`. The JSON field is called `created_at`, the record component is `createdAt`, and Jackson silently ignored the field it could not match.
* You could annotate the component with `@JsonProperty("created_at")` as in the OpenAI lab, but every Ollama response has snake_case names, so set the naming strategy once on the mapper instead:

```java
private final ObjectMapper mapper = JsonMapper.builder()
    .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    .enable(SerializationFeature.INDENT_OUTPUT)
    .build();
```

* Run the test again to see that the field is populated, and add an assertion that `createdAt` is not null. The strategy works in both directions, so record components named `maxTokens` would be written as `max_tokens` in requests as well.

## Create a vision request

* Ollama also supports vision requests, where the model can read images and generate text descriptions from them. The images must be uploaded in the form of Base 64 encoded strings.
* Conveniently, `gemma4` is already multimodal, so we don't need to pull a separate vision model -- we'll send the image to the same model we've been using for text.
* Add a new record to `OllamaRecords` called `OllamaVisionRequest` with the following fields:
  - `model` of type `String`
  - `prompt` of type `String`
  - `stream` of type `boolean`
  - `images` of type `List<String>`

```java
public record OllamaVisionRequest(
        String model,
        String prompt,
        boolean stream,
        List<String> images) {}
```

* Fortunately, the output response from vision models is the same as from the text models, so you can reuse the `OllamaResponse` record.
* Rather than require the client to submit the image in that form, let's provide a method to convert a local image path into the proper string. We'll call this method inside a _compact constructor_, a cool feature of records.
* Inside the `OllamaVisionRequest` record, add a compact constructor that takes a `String` path to a local image and converts it to a Base 64 encoded string.

```java
public record OllamaVisionRequest(
        String model,
        String prompt,
        boolean stream,
        List<String> images) {

    public OllamaVisionRequest {
        images = images.stream()
                    .map(this::encodeImage)
                    .collect(Collectors.toList());
    }

    private String encodeImage(String path) {
        try {
            byte[] imageBytes = Files.readAllBytes(Paths.get(path));
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
```

* Add a new method to `OllamaService` called `generateVision` that takes an `OllamaVisionRequest` object and returns an `OllamaResponse` object.
* We need an image to view. Here is one if you don't already have one available:

![Cats playing cards](src/main/resources/cats_playing_cards.png)

* Add the following test to the `OllamaServiceTest` class:

```java

@Test
void test_vision_generate() {
    var request = new OllamaVisionRequest("gemma4",
            """
            Generate a text description of this image
            suitable for accessibility in HTML.
            """,
            false,
            List.of("src/main/resources/cats_playing_cards.png"));
    OllamaResponse ollamaResponse = service.generateVision(request);
    assertNotNull(ollamaResponse);
    System.out.println(ollamaResponse.response());
}
```

* Add the `generateVision` method to the `OllamaService` class:

```java
public OllamaResponse generateVision(OllamaVisionRequest visionRequest) {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(URL + "/api/generate"))
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(
                mapper.writeValueAsString(visionRequest)))
        .build();
    try {
        HttpResponse<String> response = requireSuccess(client.send(request, HttpResponse.BodyHandlers.ofString()));
        return mapper.readValue(response.body(), OllamaResponse.class);
    } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```

* The result is inside the `response` field of the `OllamaResponse` object. Run the test to see the result.

* That's an awful lot of duplicated code. Surely we can do better? We can, by using a _sealed interface_.

## Refactor to sealed interfaces

* Now comes the question: how do we send the vision request to the service? We'd rather not duplicate all the existing code. Fortunately, records can implement interfaces. In this case, we'll use a _sealed interface_, so that only _permitted_ classes can implement it.
* Refactor our existing code by creating a sealed interface called `OllamaRequest` with two permitted classes: `OllamaTextRequest` and `OllamaVisionRequest` and add it to our `OllamaRecords` class. Add `implements OllamaRequest` to both records.
* Make sure the tests for the text model still pass.

```java
public class OllamaRecords {
    public sealed interface OllamaRequest
            permits OllamaTextRequest, OllamaVisionRequest {
    }

    public record OllamaTextRequest(
            String model,
            String prompt,
            boolean stream) implements OllamaRequest {}

    public record OllamaVisionRequest(
            String model,
            String prompt,
            boolean stream,
            List<String> images) implements OllamaRequest {

        public OllamaVisionRequest {
            images = images.stream()
                    .map(this::encodeImage)
                    .collect(Collectors.toList());
        }

        private String encodeImage(String path) {
            try {
                byte[] imageBytes = Files.readAllBytes(Paths.get(path));
                return Base64.getEncoder()
                        .encodeToString(imageBytes);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
```

* In `OllamaService`, we can go back to when our `generate` method took an `OllamaRequest` object as its only argument and let polymorphism distinguish between a text request and a vision request. We can then use pattern matching to determine which type of request we have and act accordingly. We had this before, but let's repeat it here for convenience:

```java
public OllamaResponse generate(OllamaRequest ollamaRequest) {
    HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(URL + "/api/generate"))
          .header("Content-Type", "application/json")
          .header("Accept", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(ollamaRequest)))
          .build();
    try {
        HttpResponse<String> response = requireSuccess(client.send(request, HttpResponse.BodyHandlers.ofString()));
        return mapper.readValue(response.body(), OllamaResponse.class);
    } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```

* Pattern matching for switch became GA in Java 21. If you have Java 21 available, let's use it to identify what type of request we are sending. First, add a system logger as an attribute to `OllamaService`:

```java
private final System.Logger logger = System.getLogger(OllamaService.class.getName());
```

* With that available, we can now add a switch statement to the beginning of the `generate` method:

```java
public OllamaResponse generate(OllamaRequest ollamaRequest) {
    switch (ollamaRequest) {
        case OllamaTextRequest textRequest -> {
            logger.log(System.Logger.Level.INFO, "Text request to: {0}", textRequest.model());
            logger.log(System.Logger.Level.INFO, "Prompt: {0}", textRequest.prompt());
        }
        case OllamaVisionRequest visionRequest -> {
            logger.log(System.Logger.Level.INFO, "Vision request to: {0}", visionRequest.model());
            logger.log(System.Logger.Level.INFO, "Size of uploaded image: {0}", visionRequest.images()
                    .getFirst()
                    .length());
            logger.log(System.Logger.Level.INFO, "Prompt: {0}", visionRequest.prompt());
        }
    }

    // ... rest as before ...
}
```

* Because we used a sealed interface, there are only two possible types of requests. Our switch expression is therefore exhaustive. We can add tests for each to verify that the logging works as expected.

```java
@Test
void generate_with_text_request() {
  var ollamaRequest = new OllamaTextRequest("gemma4", "Why is the sky blue?", false);
  OllamaResponse ollamaResponse = service.generate(ollamaRequest);
  System.out.println(ollamaResponse);
  String answer = ollamaResponse.response();
  System.out.println(answer);
  assertTrue(answer.contains("scattering"));
  assertNotNull(ollamaResponse.createdAt());
}

@Test
void generate_with_vision_request() {
  var request = new OllamaVisionRequest("gemma4",
          """
                  Generate a text description of this image
                  suitable for accessibility in HTML.
                  """,
          false,
          List.of("src/main/resources/cats_playing_cards.png"));
  OllamaResponse response = service.generate(request);
  assertNotNull(response);
  System.out.println(response);
}
```

* Run the tests to verify that the logging works as expected. This combination of modeling the data as records, limiting the available entries with sealed interfaces, and selecting between them with switch expressions, is a form of [data oriented programming](https://www.infoq.com/articles/data-oriented-programming-java/), as described in the linked article by Brian Goetz.

## Have a conversation

* The `generate` endpoint we've been using is very limited. Ollama also supports a more complicated model, similar to those from OpenAI, Anthropic, Gemini, and Mistral. That model allows _conversations_, that connect messages together. You need to add the messages yourself in a list, alternating between `user` and `assistant` messages, and the AI tool will respond to the complete sequence.
* In Ollama, you send a POST request to `/api/chat` instead of `/api/generate` when you're dealing with a list of messages rather than just a single one.
* Here is a sample request (from the [Ollama documentation](https://github.com/ollama/ollama/blob/main/docs/api.md)):

```json
{
    "model": "gemma4",
    "messages": [
      {
        "role": "user",
        "content": "why is the sky blue?"
      },
      {
        "role": "assistant",
        "content": "due to rayleigh scattering."
      },
      {
        "role": "user",
        "content": "how is that different than mie scattering?"
      }
    ]
}
```

* Create a new record called `OllamaMessage` with the following fields:
  - `role` of type `String`
  - `content` of type `String`

```java
public record OllamaMessage(
        String role,
        String content) {}
```

* The value of `role` is restricted to:
  - `user`, for client questions
  - `assistant`, for AI responses
  - `system`, for global context
- With that in mind, add a compact constructor to `OllamaMessage` that validates the `role` field:

```java
public record OllamaMessage(String role,
                            String content) {
    public OllamaMessage {
        if (!List.of("user", "assistant", "system").contains(role)) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}
```

* Create a new record called `OllamaChatRequest` with the following fields:
  - `model` of type `String`
  - `messages` of type `List<OllamaMessage>`
  - 'temperature' of type `double`

```java
public record OllamaChatRequest(
        String model,
        List<OllamaMessage> messages,
        boolean stream) {}
```


* Add a record called `OllamaChatResponse` with the following fields:
  - `model` of type `String`
  - `createdAt` of type `String`
  - `message` of type `OllamaMessage`
  - `done` of type `boolean`

```java
public record OllamaChatResponse(
        String model,
        String createdAt,
        OllamaMessage message,
        boolean done) {}
```

* Add a new method to `OllamaService` called `chat` that takes an `OllamaChatRequest` object and returns a `List<OllamaMessage>` object.
* Add a test to try out the conversation model:

```java
@Test
void test_chat() {
    var request = new OllamaChatRequest("gemma4",
            List.of(new OllamaMessage("user", "why is the sky blue?"),
                    new OllamaMessage("assistant", "due to rayleigh scattering."),
                    new OllamaMessage("user", "how is that different than mie scattering?")),
            false);
    OllamaChatResponse response = service.chat(request);
    assertNotNull(response);
    System.out.println(response);
}
```

* Update the `OllamaService` class to include the `chat` method:

```java
public OllamaChatResponse chat(OllamaChatRequest chatRequest) {
    String json = mapper.writeValueAsString(chatRequest);
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(URL + "/api/chat"))
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();
    try {
        HttpResponse<String> response = requireSuccess(client.send(request, HttpResponse.BodyHandlers.ofString()));
        return mapper.readValue(response.body(), OllamaChatResponse.class);
    } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```

* Run the test to see it in action. In practice, you would extract the `assistant` response from each message and add it to the next request. Note that all the major framework (Spring AI, LangChain4j, etc) have ways of doing that for you.

## Call cloud models through OpenRouter

The earlier labs built HTTP requests by hand. LangChain4j wraps that work in a `ChatModel` interface, and OpenRouter lets one API key reach models from many vendors. Together they make switching models a one-line change.

OpenRouter exposes an OpenAI-compatible API at `https://openrouter.ai/api/v1`, so LangChain4j's `OpenAiChatModel` works against it with a different base URL, key, and a provider-prefixed model id such as `openai/gpt-5-nano` or `google/gemini-3-flash-preview`.

* Create an account at [openrouter.ai](https://openrouter.ai), generate an API key, and add it to an environment variable called `OPENROUTER_API_KEY`. Restart your IDE or terminal so the new variable is visible.

* Create a class called `QuickChatDemo` in the `com.kousenit.demos` package:

```java
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import java.util.Objects;

public class QuickChatDemo {
    private static final String OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1";

    public static void main(String[] args) {
        String apiKey = Objects.requireNonNull(
                System.getenv("OPENROUTER_API_KEY"), "Set the OPENROUTER_API_KEY environment variable");

        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(OPENROUTER_BASE_URL)
                .apiKey(apiKey)
                .modelName("openai/gpt-5-nano")
                .build();

        String prompt = "Explain what LangChain4j is in 2-3 sentences.";
        System.out.println(model.chat(prompt));
    }
}
```

* Run the class. Compare this to the `HttpClient` code from the first labs: the request body, headers, and JSON parsing are all handled by the builder and the `chat` method.

* Now look at what the builder did for you. Add `.logRequests(true)` and `.logResponses(true)` to the builder and run it again. LangChain4j logs the exact HTTP exchange:

```
HTTP request:
- method: POST
- url: https://openrouter.ai/api/v1/chat/completions
- headers: [Authorization: Beare...4f], [Content-Type: application/json], [User-Agent: langchain4j-openai]
- body: {
  "model" : "openai/gpt-5-nano",
  "messages" : [ {
    "role" : "user",
    "content" : "Explain what LangChain4j is in 2-3 sentences."
  } ],
  "stream" : false
}
```

* That is the same request you built by hand in the Ollama conversation lab: a model name and a list of `role`/`content` messages, posted as JSON with a bearer token. The framework saved you the records, the mapper, and the status check, but it did not do anything you could not do yourself. Note that the key is masked in the log.

* Read the response log too. Beyond `choices[0].message.content`, the `usage` block separates `completion_tokens` from `reasoning_tokens` (gpt-5-nano is a reasoning model, and the reasoning can cost more than the answer), and OpenRouter adds a `cost` field in dollars. When a framework call behaves strangely, this log is the first place to look.

* Now create `MultiModelDemo` in the same package. Add a helper that builds a model for any OpenRouter model id:

```java
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class MultiModelDemo {
    private static final String OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1";

    private static ChatModel openRouter(String modelId) {
        return OpenAiChatModel.builder()
                .baseUrl(OPENROUTER_BASE_URL)
                .apiKey(System.getenv("OPENROUTER_API_KEY"))
                .modelName(modelId)
                .build();
    }
}
```

* Add a `main` method that asks the same question of several models. Wrap each call in a `try`/`catch` so one unavailable model does not stop the others:

```java
public static void main(String[] args) {
    String question = "What is the capital of France? Answer in exactly one word.";

    for (String modelId : new String[] {"openai/gpt-5-nano", "google/gemini-3-flash-preview"}) {
        try {
            System.out.printf("%s: %s%n", modelId, openRouter(modelId).chat(question));
        } catch (Exception e) {
            System.out.printf("%s: not available - %s%n", modelId, e.getMessage());
        }
    }

    try {
        ChatModel ollama = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("gemma4")
                .build();
        System.out.printf("ollama/gemma4 (local): %s%n", ollama.chat(question));
    } catch (Exception e) {
        System.out.println("ollama/gemma4 (local): not available - is Ollama running?");
    }
}
```

* Run it. Then pick any other chat model id from [openrouter.ai/models](https://openrouter.ai/models) and add it to the array. No other code changes are needed.

* Each `chat` call blocks on the network for a second or more, and the three run one after another. Nothing about them depends on each other, so run them at the same time. Each call is a blocking HTTP request that spends almost all of its time waiting, which is the work virtual threads are for: one thread per call, no pool to size. Collect the models in a map and submit each call to a virtual-thread executor:

```java
Map<String, ChatModel> models = new LinkedHashMap<>();
models.put("openai/gpt-5-nano", openRouter("openai/gpt-5-nano"));
models.put("google/gemini-3-flash-preview", openRouter("google/gemini-3-flash-preview"));
models.put("gemma4 (local)", OllamaChatModel.builder()
        .baseUrl("http://localhost:11434").modelName("gemma4").build());

Instant start = Instant.now();
Map<String, Future<String>> answers = new LinkedHashMap<>();
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    models.forEach((name, model) ->
            answers.put(name, executor.submit(() -> model.chat(question))));
}   // close() waits for every task, so each future below is already complete
answers.forEach((name, future) ->
        System.out.printf("%s: %s%n", name, future.resultNow()));
System.out.printf("Concurrent: %d ms%n", Duration.between(start, Instant.now()).toMillis());
```

* Time the sequential loop the same way and compare. The concurrent version takes about as long as the slowest single call. A few things to notice:
  - The code that calls the model did not change. `chat` still blocks; the virtual thread parks while it waits and costs almost nothing to keep around.
  - `Executors.newVirtualThreadPerTaskExecutor()` is `AutoCloseable`, and `close()` waits for the submitted tasks, so the futures are done by the time the block ends. `resultNow()` reads them without checked exceptions.
  - With a platform-thread pool you would be sizing the pool against your rate limits. With virtual threads, the provider's rate limit is the only limit you manage.
  - The same shape works for any fan-out: one question to many models, many documents to one model, or a batch of embeddings.

* The solution's `MultiModelDemo` runs both versions and prints the timings.

* Notice the two different kinds of portability in this class:
  - Switching between the OpenRouter models is a **model id** change. The client class stays `OpenAiChatModel` because OpenRouter speaks the OpenAI protocol.
  - Switching to Ollama is a **client class** change. `OllamaChatModel` speaks a different protocol, but LangChain4j's `ChatModel` interface means the call to `chat` looks the same.

* The solution classes `QuickChatDemo` and `MultiModelDemo` are in the `demos` package of the repository if you want to compare.

## Add tool calling with LangChain4j

Tool calling lets the model ask your Java application to run specific methods. This is the bridge from "chatbot" to application integration: the model decides that it needs data or an action, but Java still owns the actual method, validation, and side effects.

In this lab, create a small travel assistant that can call Java methods for weather, driving distance, and fuel cost.

* Add these imports to a new class called `ToolCallingDemo` in your demos package:

```java
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
```

* Add an AI service interface. The system message tells the model when tools are expected:

```java
interface TravelAssistant {
    @SystemMessage("""
        You are a practical travel assistant.
        Use the provided Java tools whenever the user asks about
        weather, distance, drive time, or estimated travel cost.
        Explain which tool results shaped your answer.
        """)
    String chat(String message);
}
```

* Add a `TravelTools` class with ordinary Java methods annotated with `@Tool`:

```java
public static class TravelTools {
    private static final Map<String, Weather> WEATHER = Map.of(
            "hartford", new Weather("Hartford, CT", 72, "partly cloudy"),
            "boston", new Weather("Boston, MA", 66, "cool and breezy"),
            "new york", new Weather("New York, NY", 75, "warm and humid"));

    @Tool("Get a small weather summary for a city")
    public String weather(@P("city name, such as Hartford or Boston") String city) {
        Weather weather = WEATHER.get(city.toLowerCase().trim());
        if (weather == null) {
            return "No weather data available for " + city;
        }
        return "%s: %d F and %s".formatted(
                weather.city(), weather.temperatureF(), weather.summary());
    }

    @Tool("Estimate fuel cost for a trip")
    public String fuelCost(
            @P("driving distance in miles") int miles,
            @P("vehicle fuel economy in miles per gallon") double mpg,
            @P("fuel price per gallon") double pricePerGallon) {
        BigDecimal gallons = BigDecimal.valueOf(miles)
                .divide(BigDecimal.valueOf(mpg), 2, RoundingMode.HALF_UP);
        BigDecimal cost = gallons.multiply(BigDecimal.valueOf(pricePerGallon))
                .setScale(2, RoundingMode.HALF_UP);
        return "%s gallons, about $%s".formatted(gallons, cost);
    }

    private record Weather(String city, int temperatureF, String summary) {}
}
```

* Add a distance tool. This is intentionally a Java method, not a prompt trick:

```java
@Tool("Estimate driving distance in miles between two supported cities")
public int drivingDistance(
        @P("starting city") String from,
        @P("destination city") String to) {
    if (from.equalsIgnoreCase("Hartford") && to.equalsIgnoreCase("Boston")) {
        return 102;
    }
    if (from.equalsIgnoreCase("Hartford") && to.equalsIgnoreCase("New York")) {
        return 116;
    }
    throw new IllegalArgumentException("Unsupported route: " + from + " to " + to);
}
```

* Build the model and register the tools with `AiServices`. Leave the temperature at its default: `gpt-5-nano` is a reasoning model and rejects any other value.

```java
ChatModel model = OpenAiChatModel.builder()
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .modelName("gpt-5-nano")
        .build();

TravelAssistant assistant = AiServices.builder(TravelAssistant.class)
        .chatModel(model)
        .tools(new TravelTools())
        .build();
```

* Ask a question that requires multiple tools:

```java
String answer = assistant.chat("""
        I am driving from Hartford to Boston.
        What is the weather like at both ends,
        how far is the trip, and what would fuel cost
        at 30 mpg and $3.65 per gallon?
        """);
System.out.println(answer);
```

* Run the completed demo:

```bash
./gradlew run -PmainClass=com.kousenit.demos.ToolCallingDemo
```

The important design point is that the model does not get direct access to your system. It gets a tool specification. LangChain4j executes the selected Java method and returns the result to the model. In production, validate every argument, keep side effects explicit, and log each tool call separately from the model's final prose answer.

* To see exactly what that means, set `.logRequests(true)` and `.logResponses(true)` on the model builder and run the demo again. Three HTTP exchanges go by. The first request carries your Java methods as a `tools` array, a JSON Schema that LangChain4j derived from the `@Tool` and `@P` annotations and the parameter types:

```json
"tools" : [ {
  "type" : "function",
  "function" : {
    "name" : "drivingDistance",
    "description" : "Estimate driving distance in miles between two supported cities",
    "parameters" : {
      "type" : "object",
      "properties" : {
        "from" : { "type" : "string", "description" : "starting city" },
        "to" :   { "type" : "string", "description" : "destination city" }
      },
      "required" : [ "from", "to" ]
    }
  }
}, ... ]
```

* The first response has no answer. Its `finish_reason` is `tool_calls`, and the message asks your application to run three methods, with the arguments as a JSON string:

```json
"message": {
  "role": "assistant",
  "content": null,
  "tool_calls": [
    { "id": "call_nWgr...", "type": "function",
      "function": { "name": "weather", "arguments": "{\"city\": \"Hartford\"}" } },
    { "id": "call_O8fc...", "type": "function",
      "function": { "name": "weather", "arguments": "{\"city\": \"Boston\"}" } },
    { "id": "call_NqbU...", "type": "function",
      "function": { "name": "drivingDistance", "arguments": "{\"from\": \"Hartford\", \"to\": \"Boston\"}" } }
  ]
},
"finish_reason": "tool_calls"
```

* LangChain4j calls the Java methods, then sends the whole conversation back with the assistant's request and one `tool` message per result, each tied to its call by id:

```json
{ "role" : "assistant", "tool_calls" : [ ... the three calls above ... ] },
{ "role" : "tool", "tool_call_id" : "call_nWgr...", "content" : "Hartford, CT: 72 F and partly cloudy" },
{ "role" : "tool", "tool_call_id" : "call_O8fc...", "content" : "Boston, MA: 66 F and cool and breezy" },
{ "role" : "tool", "tool_call_id" : "call_NqbU...", "content" : "102" }
```

* The model now has the distance, so the second response asks for `fuelCost` with `miles` set to 102. The third exchange returns the prose answer. Three things to take away:
  - The model never executes anything. It emits JSON saying which method it wants and with what arguments. Your code decides whether to run it.
  - The "loop" is a plain HTTP loop: send, read `finish_reason`, run methods, append `tool` messages, send again. LangChain4j wrote it so you do not have to, but there is no magic in it.
  - Every argument arrives as a string the model composed. `drivingDistance("Hartford", "Boston")` worked because the model chose well; validate as if a user typed it.

* Turn the logging back off before moving on, or leave it on while you work through the next lab: the structured output request carries a `response_format` with the JSON Schema that LangChain4j generated from the `CourseInquiry` record, which is worth a look.

## Extract structured output into Java records

Tool calling is useful when the model needs your application to do something. Structured output is useful when your application needs the model to return data your code can consume safely.

In this lab, extract a structured `CourseInquiry` record from an unstructured message. LangChain4j can generate a JSON Schema from the return type, ask the model for JSON that matches it, and parse the result back into a Java record.

* Add a class called `StructuredOutputDemo` in the demos package.
* Add these imports:

```java
import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.AiServices;
import java.util.List;
```

* Add an enum for priority values:

```java
public enum Priority {
    LOW,
    MEDIUM,
    HIGH
}
```

* Add a record for the extracted data. Use `@JsonProperty(required = true)` for required schema fields and `@Description` to give the model field-level guidance:

```java
@Description("A structured summary of a student or customer request")
public record CourseInquiry(
        @JsonProperty(required = true)
        @Description("Short topic label")
        String topic,

        @JsonProperty(required = true)
        Priority priority,

        @JsonProperty(required = true)
        @Description("Whether a human should follow up")
        boolean needsFollowUp,

        @JsonProperty(required = true)
        @Description("Concrete next actions")
        List<String> actionItems) {}
```

* Add an AI service interface that returns the record rather than a `String`:

```java
interface InquiryExtractor {
    CourseInquiry extract(String message);
}
```

* Configure the model with JSON Schema support enabled:

```java
ChatModel model = OpenAiChatModel.builder()
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .modelName("gpt-5-nano")
        .supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
        .strictJsonSchema(true)
        .build();
```

* Build the AI service:

```java
InquiryExtractor extractor = AiServices.builder(InquiryExtractor.class)
        .chatModel(model)
        .build();
```

* Extract the record from an unstructured message:

```java
CourseInquiry inquiry = extractor.extract("""
        A student says: I finished the tool calling lab, but I am still
        confused about the difference between tool calling and structured
        outputs. Could you add a small example before tomorrow?
        """);

System.out.println(inquiry);
```

* Add application-side validation. Structured output is not a substitute for validation:

```java
public static void validate(CourseInquiry inquiry) {
    if (inquiry.topic() == null || inquiry.topic().isBlank()) {
        throw new IllegalArgumentException("topic is required");
    }
    if (inquiry.priority() == null) {
        throw new IllegalArgumentException("priority is required");
    }
    if (inquiry.actionItems() == null || inquiry.actionItems().isEmpty()) {
        throw new IllegalArgumentException("at least one action item is required");
    }
}
```

* Run the completed demo:

```bash
./gradlew run -PmainClass=com.kousenit.demos.StructuredOutputDemo
```

* Add a cheap unit test that does not call the model. This verifies the Java-side parse and validation behavior:

```java
private final ObjectMapper mapper = JsonMapper.builder().build();  // tools.jackson.databind.json.JsonMapper

@Test
void parses_structured_output_into_record() throws Exception {
    String json = """
        {
          "topic": "Tool calling versus structured outputs",
          "priority": "HIGH",
          "needsFollowUp": true,
          "actionItems": [
            "Add a small structured-output demo",
            "Explain when to use tools versus JSON records"
          ]
        }
        """;

    CourseInquiry inquiry = mapper.readValue(json, CourseInquiry.class);
    StructuredOutputDemo.validate(inquiry);
    assertThat(inquiry.priority()).isEqualTo(Priority.HIGH);
}
```

The design point is the same as with tool calling: let the model help with interpretation, but keep your application responsible for contracts. In production, validate the parsed object and decide what to do when required fields are missing, enum values are wrong, or the extracted data is semantically questionable.

## Generating Images

OpenAI's current image models are the GPT Image 2.5 pair: `gpt-image-2.5-flare` is the fast, everyday generator and `gpt-image-2.5-sunburst` is tuned for precise edits. The lab uses Flare. Both take the same request shape, so switching is a one-string change.

* We now return to OpenAI to use the GPT Image model. Generating images is straightforward. Again, we'll create an image request object that models the input JSON data, send it in a POST request, and process the results. GPT Image returns Base 64 encoded image data from the Image API.

* Add records for image generation to the `OpenAiRecords` class:

```java
public record ImageRequest(
        String model,
        String prompt,
        Integer n,
        String quality,
        String outputFormat,
        String size
) {}
```

* The response wraps the generated image as Base 64 encoded data. Add the response record:

```java
public record ImageResponse(
        long created,
        List<Image> data) {
  public record Image(
          String b64Json,
          String revisedPrompt) {}
}
```

* Add a class called `GptImageService` with constants for the endpoint, the API key, an `HttpClient`, and an `ObjectMapper`. The `Integer n` component of `ImageRequest` may be null, and the API rejects `"n": null`, so this mapper also drops null fields when writing:

```java
public class GptImageService {
  private static final String IMAGE_URL = "https://api.openai.com/v1/images/generations";
  private static final String API_KEY = System.getenv("OPENAI_API_KEY");

  // Reuse HttpClient for better performance through connection pooling
  private static final HttpClient client = HttpClient.newHttpClient();

  // SNAKE_CASE writes outputFormat as output_format and reads b64_json into b64Json
  private final ObjectMapper mapper = JsonMapper.builder()
          .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
          .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
          .build();
}
```

* Add a new method to `GptImageService` called `generateImage` that takes a `ImageRequest` object and returns a `ImageResponse` object:

```java
public ImageResponse generateImage(ImageRequest imageRequest) {
  HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(IMAGE_URL))
          .header("Authorization", "Bearer %s".formatted(API_KEY))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(imageRequest)))
          .build();
  try {
    HttpResponse<String> response =
            requireSuccess(client.send(request, HttpResponse.BodyHandlers.ofString()));
    return mapper.readValue(response.body(), ImageResponse.class);
  } catch (IOException | InterruptedException e) {
    throw new RuntimeException("Error sending image prompt", e);
  }
}
```

* Add a test to try out the image generation model:

```java
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static com.kousenit.OpenAiRecords.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GptImageServiceTest {
    private final GptImageService service = new GptImageService();

    @Test
    void generate_image() {
        var imageRequest = new ImageRequest(
            "gpt-image-2.5-flare",
            "Draw a picture of cats playing cards",
            1,
            "medium",
            "png",
            "1024x1024");
        ImageResponse imageResponse = service.generateImage(imageRequest);
        System.out.println(imageResponse.data().getFirst().revisedPrompt());
        System.out.println("Base64 image bytes: " +
            imageResponse.data().getFirst().b64Json().length());
    }
}
```

* Run the test to see it in action. The response contains Base 64 encoded image data and the revised prompt. In a full application, decode the `b64Json` value and write the bytes to a `.png` file.

## Clean Streaming with LangChain4j

LangChain4j provides utility methods in `LambdaStreamingResponseHandler` that make streaming much cleaner than implementing the full `StreamingChatResponseHandler` interface.

### Simple Streaming

For basic streaming where you just want to print tokens as they arrive:

```java
import static dev.langchain4j.model.LambdaStreamingResponseHandler.onPartialResponseBlocking;

var model = OllamaStreamingChatModel.builder()
        .baseUrl("http://localhost:11434")
        .modelName("gemma4")
        .build();

// One-liner for simple streaming; returns once the response is complete
onPartialResponseBlocking(model, "Tell me a joke", System.out::print);
```

Streaming is asynchronous. The plain `model.chat(prompt, onPartialResponse(...))` form returns immediately, which is what you want inside a web request handler, but in a `main` method the program exits before any tokens arrive. The `*Blocking` variants wait for the stream to finish, so use them in standalone demos.

### Streaming with Error Handling

When you need to handle both partial responses and errors:

```java
import static dev.langchain4j.model.LambdaStreamingResponseHandler.onPartialResponseAndErrorBlocking;

onPartialResponseAndErrorBlocking(model, "Why is the sky blue?",
    token -> System.out.print(token),           // Handle each token
    error -> System.err.println("Error: " + error)  // Handle errors
);
```

### Benefits

This approach eliminates the need for:
- Anonymous inner classes
- CompletableFuture boilerplate
- Implementing unused interface methods

The utility methods provide a functional, lambda-based approach that's much cleaner and more maintainable.

### Demo

Run the `StreamingDemo` class to see both patterns in action:

```bash
./gradlew run -PmainClass=com.kousenit.demos.StreamingDemo
```

## Going Further: Decision Models

The structured output lab asked a chat model to fill in a Java record. That gives you one answer per field, but not how sure the model was. LangChain4j 1.23 adds an **experimental** `DecisionModel` API for exactly that gap: you send an input and a set of typed questions, and the model returns one answer per question with a probability for every option. There is no text to parse, and the thresholds that turn probabilities into actions stay in your Java code.

Three question types are available:

- `YesNoQuestion` returns the probability of "yes". Call `isYes(threshold)` to decide.
- `ChoiceQuestion` picks one option from a named set and reports the probability of each. `margin()` is the gap between the top two, which is a good "ask a human" signal.
- `ScaleQuestion` places the input on an ordered scale and reports a probability-weighted `mean()`.

The project already includes the `langchain4j-typesafe` module. Its `TypeSafeDecisionModel` talks to TypeSafe's Jev model, and OpenRouter serves the same model, so the `OPENROUTER_API_KEY` from the earlier lab is all you need:

```java
DecisionModel model = TypeSafeDecisionModel.builder()
        .baseUrl("https://openrouter.ai/api")   // the client appends v1/systemone itself
        .apiKey(System.getenv("OPENROUTER_API_KEY"))
        .modelName("typesafe/jev-1.13")
        .build();
```

The `DecisionModelDemo` class in the `demos` package triages three training inquiries with a choice question (which course), a yes/no question (does the sender ask for a next step), and a scale question (how urgent). Run it and read the probabilities next to each answer:

```bash
./gradlew run -PmainClass=com.kousenit.demos.DecisionModelDemo
```

Things to keep in mind if you take this further:

- The API is marked experimental, so method names may change between LangChain4j releases.
- Probabilities are calibrated per model. A threshold tuned for one model or version needs retuning for another, so record `response.modelName()` alongside each decision.
- The input is sent to the model provider like any other request. Send only what the decision needs.
- A decision model is not a security control on its own. Inputs can contain text written to influence the answer.

## Going Further: JSON Parsing Strategies

Every lab so far mapped JSON straight into records, which is the right default when you know the shape of the data. Some APIs return shapes that vary from call to call. OpenAI's Responses API, for example, returns an `output` array whose items can be `reasoning`, `web_search_call`, or `message`, in an order that depends on the model and the tools it used. Jackson's tree model handles that case.

### Records when the shape is known

```java
ModelList models = mapper.readValue(response.body(), ModelList.class);
```

One line, type-safe, and unknown fields are ignored. This is data binding, and it is what the labs use.

### JsonNode when the shape varies

```java
JsonNode root = mapper.readTree(response.body());

// JSON Pointer (RFC 6901) for a value whose location never changes
String id = root.at("/id").asString();
int outputTokens = root.at("/usage/output_tokens").asInt();

// A walk for an array whose contents vary
for (JsonNode item : root.path("output")) {
    if ("message".equals(item.path("type").asString())) {
        String text = item.at("/content/0/text").asString("(no text)");
    }
}
```

`at()` takes a pointer and returns a missing node rather than throwing when the path does not exist; `path()` does the same one step at a time. Resist the temptation to write `root.at("/output/0/content/0/text")` for the Responses API: with a reasoning model, item 0 is the reasoning block, and the pointer quietly returns nothing.

The `ResponsesApiDemo` class uses both styles against the live API and prints the item types it found:

```bash
./gradlew run -PmainClass=com.kousenit.demos.ResponsesApiDemo
```

### Which library?

- **Jackson 3** (`tools.jackson`) is what this course and Spring Boot 4 use. Mappers are immutable and built with `JsonMapper.builder()`, exceptions are unchecked, and `java.time` types work without extra modules.
- **Jackson 2** (`com.fasterxml.jackson`) is still everywhere, including inside LangChain4j. The two lines coexist on one classpath because the packages differ, and the annotations are shared.
- **Gson** works fine for the patterns in these labs, but its README describes it as in maintenance mode.
- **JEP 540** proposes a Simple JSON API in the JDK itself as an incubator module, currently targeted at JDK 28. Expect it to cover the tree-model style first.

Understanding raw HTTP and JSON parsing lets you use new APIs immediately without waiting for framework updates, debug responses when things go wrong, and verify what a framework is sending when its behavior is surprising.
