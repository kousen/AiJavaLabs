package com.kousenit;

import static com.kousenit.OllamaRecords.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;
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
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
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
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
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
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(response.body(), OllamaChatResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateStreamingResponse(OllamaTextRequest ollamaRequest) {
        try {
            var requestBody = mapper.writeValueAsString(ollamaRequest);
            var httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(URL + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            var responseFuture = client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                    .thenApply(this::handleLines);

            return responseFuture.join();
        } catch (Exception e) {
            System.err.println("Error generating streaming response: " + e.getMessage());
            return "";
        }
    }

    private String handleLines(HttpResponse<Stream<String>> response) {
        var accumulatedResponse = new StringBuilder();
        response.body().forEach(line -> {
            try {
                var ollamaResponse = mapper.readValue(line, OllamaResponse.class);
                System.out.print(ollamaResponse.response());
                accumulatedResponse.append(ollamaResponse.response());

                if (ollamaResponse.done()) {
                    System.out.println();
                    System.out.println("Final response metadata: " + line);
                }
            } catch (Exception e) {
                System.err.println("Error processing JSON: " + e.getMessage());
            }
        });
        return accumulatedResponse.toString();
    }
}
