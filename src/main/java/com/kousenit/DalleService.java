package com.kousenit;

import static com.kousenit.OpenAiRecords.*;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DalleService {
    private static final String IMAGE_URL = "https://api.openai.com/v1/images/generations";
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public ImageResponse generateImage(ImageRequest imageRequest) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(IMAGE_URL))
                .header("Authorization", "Bearer %s".formatted(API_KEY))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(imageRequest)))
                .build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), ImageResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error sending prompt prompt", e);
        }
    }
}
