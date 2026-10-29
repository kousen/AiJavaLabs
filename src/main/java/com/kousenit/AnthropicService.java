package com.kousenit;

import static com.kousenit.AnthropicRecords.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

/**
 * Same pattern as OpenAiService, different vendor: Anthropic authenticates with an
 * x-api-key header plus a version header instead of a Bearer token. Jackson 3 reads
 * the created_at timestamp into a java.time.Instant without extra configuration.
 */
public class AnthropicService {
    private static final String API_KEY = System.getenv("ANTHROPIC_API_KEY");
    private static final String MODELS_URL = "https://api.anthropic.com/v1/models";

    // HttpClient is thread-safe and designed to be reused for multiple requests
    // Using a single instance improves performance through connection pooling
    private static final HttpClient client = HttpClient.newHttpClient();

    private final ObjectMapper mapper = JsonMapper.builder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();

    public ModelsResponse listModels() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MODELS_URL))
                .header("x-api-key", API_KEY)
                .header("anthropic-version", "2023-06-01")
                .header("Accept", "application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(response.body(), ModelsResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
