package com.kousenit;

import static com.kousenit.HttpSupport.requireSuccess;
import static com.kousenit.OpenAiRecords.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

public class OpenAiService {
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String MODELS_URL = "https://api.openai.com/v1/models";

    // HttpClient is thread-safe and designed to be reused for multiple requests
    // Using a single instance improves performance through connection pooling
    private static final HttpClient client = HttpClient.newHttpClient();

    // Jackson 3 mappers are immutable and built once. SNAKE_CASE maps owned_by <-> ownedBy.
    private final ObjectMapper mapper = JsonMapper.builder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();

    public ModelList listModels() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MODELS_URL))
                .header("Authorization", "Bearer %s".formatted(API_KEY))
                .header("Accept", "application/json")
                .build();
        try {
            HttpResponse<String> response = requireSuccess(client.send(request, HttpResponse.BodyHandlers.ofString()));
            return mapper.readValue(response.body(), ModelList.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public VectorStoreList listVectorStores() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/vector_stores"))
                .header("Authorization", "Bearer %s".formatted(API_KEY))
                .header("OpenAI-Beta", "assistants=v2")
                .header("Accept", "application/json")
                .build();
        try {
            HttpResponse<String> response = requireSuccess(client.send(request, HttpResponse.BodyHandlers.ofString()));
            return mapper.readValue(response.body(), VectorStoreList.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteVectorStore(String id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/vector_stores/%s".formatted(id)))
                .header("Authorization", "Bearer %s".formatted(API_KEY))
                .header("OpenAI-Beta", "assistants=v2")
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
