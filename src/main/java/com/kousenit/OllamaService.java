package com.kousenit;

import static com.kousenit.HttpSupport.requireSuccess;
import static com.kousenit.OllamaRecords.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    private final System.Logger logger = System.getLogger(OllamaService.class.getName());

    // SNAKE_CASE maps created_at <-> createdAt without annotating every record
    private final ObjectMapper mapper = JsonMapper.builder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    public OllamaResponse generate(OllamaRequest ollamaRequest) {
        switch (ollamaRequest) {
            case OllamaTextRequest textRequest -> {
                logger.log(System.Logger.Level.INFO, "Text request to: {0}", textRequest.model());
                logger.log(System.Logger.Level.INFO, "Prompt: {0}", textRequest.prompt());
            }
            case OllamaVisionRequest visionRequest -> {
                logger.log(System.Logger.Level.INFO, "Vision request to: {0}", visionRequest.model());
                logger.log(
                        System.Logger.Level.INFO,
                        "Size of uploaded image: {0}",
                        visionRequest.images().getFirst().length());
                logger.log(System.Logger.Level.INFO, "Prompt: {0}", visionRequest.prompt());
            }
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL + "/api/generate"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(ollamaRequest)))
                .build();
        try {
            HttpResponse<String> response = requireSuccess(client.send(request, HttpResponse.BodyHandlers.ofString()));
            logger.log(System.Logger.Level.INFO, "Response: {0}", response.body());
            return mapper.readValue(response.body(), OllamaResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public OllamaResponse generate(String model, String input) {
        return generate(new OllamaTextRequest(model, input, false));
    }

    public String generateStreaming(OllamaTextRequest ollamaTextRequest) {
        logger.log(System.Logger.Level.INFO, "Generating streaming response with request: {0}", ollamaTextRequest);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL + "/api/generate"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(ollamaTextRequest)))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.log(System.Logger.Level.DEBUG, "Response: {0}", response.body());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public OllamaResponse generateVision(OllamaVisionRequest visionRequest) {
        logger.log(System.Logger.Level.INFO, "Model: {0}", visionRequest.model());
        logger.log(
                System.Logger.Level.INFO,
                "Size of uploaded image: {0}",
                visionRequest.images().getFirst().length());
        logger.log(System.Logger.Level.INFO, "Prompt: {0}", visionRequest.prompt());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL + "/api/generate"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(visionRequest)))
                .build();
        try {
            HttpResponse<String> response = requireSuccess(client.send(request, HttpResponse.BodyHandlers.ofString()));
            return mapper.readValue(response.body(), OllamaResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

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

    /**
     * Streams a response, handing each token to the consumer as it arrives, and returns the
     * whole text once the server sends done=true.
     *
     * The JDK does the streaming for us: sendAsync returns a CompletableFuture immediately,
     * and BodyHandlers.ofLines() delivers the body as a Stream of lines while the response is
     * still arriving. Ollama sends one JSON object per line, so each line maps to a record.
     * join() blocks until the stream ends; return the future instead if the caller should not wait.
     */
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
}
