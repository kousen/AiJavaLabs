package com.kousenit;

import com.google.gson.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

import static com.kousenit.AnthropicRecords.*;

public class AnthropicService {
    private static final String API_KEY = System.getenv("ANTHROPIC_API_KEY");
    private static final String MODELS_URL = "https://api.anthropic.com/v1/models";

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, type, ctx) ->
                    (json == null || json.isJsonNull()) ? null : Instant.parse(json.getAsString()))
            .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (src, type, ctx) ->
                    (src == null) ? null : new com.google.gson.JsonPrimitive(src.toString()))
            .create();
    
    // HttpClient is thread-safe and designed to be reused for multiple requests
    // Using a single instance improves performance through connection pooling
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Fetches the list of available Anthropic models from the API.
     *
     * Sends an HTTP GET request to the Anthropic /v1/models endpoint using
     * the API key and required headers. Parses the JSON response into a
     * {@link ModelsResponse} object using Gson.
     *
     * @return ModelsResponse containing metadata about available models
     * @throws RuntimeException if the HTTP request fails or is interrupted
     */
    public ModelsResponse listModels() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MODELS_URL))
                .header("x-api-key", API_KEY)
                .header("anthropic-version", "2023-06-01")
                .header("Accept", "application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), ModelsResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
