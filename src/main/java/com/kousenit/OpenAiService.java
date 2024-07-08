package com.kousenit;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.kousenit.OpenAiRecords.ModelList;

public class OpenAiService {
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String MODELS_URL = "https://api.openai.com/v1/models";

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public ModelList listModels() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MODELS_URL))
                .header("Authorization", "Bearer %s".formatted(API_KEY))
                .header("Accept", "application/json")
                .build();
        try (var client = HttpClient.newHttpClient()) {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), ModelList.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
