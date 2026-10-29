package com.kousenit;

import static com.kousenit.HttpSupport.requireSuccess;
import static com.kousenit.OpenRouterRecords.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

/**
 * OpenRouter's model catalog over raw HTTP. OpenRouter publishes no Java library, and none is
 * needed: it is a GET that returns JSON, and this endpoint does not even require a key.
 */
public class OpenRouterService {
    private static final String MODELS_URL = "https://openrouter.ai/api/v1/models";

    // HttpClient is thread-safe and designed to be reused for multiple requests
    private static final HttpClient client = HttpClient.newHttpClient();

    private final ObjectMapper mapper = JsonMapper.builder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();

    /** Text models, the catalog's default view. */
    public List<Model> listModels() {
        return listModels("text");
    }

    /** Models filtered by what they produce: "text", "image", "audio", "video", "decisions", or "all". */
    public List<Model> listModels(String outputModalities) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MODELS_URL + "?output_modalities=" + outputModalities))
                .header("Accept", "application/json")
                .build();
        try {
            HttpResponse<String> response = requireSuccess(client.send(request, HttpResponse.BodyHandlers.ofString()));
            return mapper.readValue(response.body(), ModelList.class).data();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
