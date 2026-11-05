package com.kousenit;

import static com.kousenit.HttpSupport.requireSuccess;
import static com.kousenit.OllamaRecords.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

public class OllamaService {
    // HttpClient is thread-safe and designed to be reused for multiple requests
    // Using a single instance improves performance through connection pooling
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String URL = "http://localhost:11434";

    // SNAKE_CASE maps created_at <-> createdAt without annotating every record
    private final ObjectMapper mapper = JsonMapper.builder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    /** Sends the whole conversation. The server keeps nothing between calls: the list is the memory. */
    public OllamaChatResponse chat(OllamaChatRequest chatRequest) {
        HttpRequest request = post("/api/chat", mapper.writeValueAsString(chatRequest));
        try {
            HttpResponse<String> response = requireSuccess(client.send(request, HttpResponse.BodyHandlers.ofString()));
            return mapper.readValue(response.body(), OllamaChatResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /** One user message, no history. */
    public OllamaChatResponse chat(String model, String prompt) {
        return chat(new OllamaChatRequest(model, List.of(OllamaMessage.user(prompt)), false));
    }

    /**
     * Streams the reply, handing each token to the consumer as it arrives, and returns the
     * whole text once the server sends done=true.
     *
     * The JDK does the streaming for us: sendAsync returns a CompletableFuture immediately,
     * and BodyHandlers.ofLines() delivers the body as a Stream of lines while the response is
     * still arriving. Ollama sends one JSON object per line, so each line maps to a record.
     * join() blocks until the stream ends; return the future instead if the caller should not wait.
     */
    public String chatStreaming(String model, List<OllamaMessage> messages, Consumer<String> onToken) {
        var chatRequest = new OllamaChatRequest(model, messages, true);
        HttpRequest request = post("/api/chat", mapper.writeValueAsString(chatRequest));

        CompletableFuture<String> fullText = client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenApply(HttpSupport::requireSuccessLines)
                .thenApply(response -> {
                    var text = new StringBuilder();
                    response.body()
                            .map(line -> mapper.readValue(line, OllamaChatResponse.class))
                            .map(chunk -> chunk.message().content())
                            .filter(token -> !token.isEmpty()) // thinking models send empty content first
                            .forEach(token -> {
                                onToken.accept(token);
                                text.append(token);
                            });
                    return text.toString();
                });

        return fullText.join();
    }

    /** Aside: Ollama's own single-prompt endpoint. No history, no roles; nothing else in the course uses it. */
    public OllamaGenerateResponse generate(String model, String prompt) {
        HttpRequest request =
                post("/api/generate", mapper.writeValueAsString(new OllamaGenerateRequest(model, prompt, false)));
        try {
            HttpResponse<String> response = requireSuccess(client.send(request, HttpResponse.BodyHandlers.ofString()));
            return mapper.readValue(response.body(), OllamaGenerateResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpRequest post(String path, String json) {
        return HttpRequest.newBuilder()
                .uri(URI.create(URL + path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }
}
