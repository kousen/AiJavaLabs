package com.kousenit;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.kousenit.OllamaRecords.*;
import static com.kousenit.OllamaRecords.OllamaTextRequest;
import static com.kousenit.OllamaRecords.OllamaResponse;

public class OllamaService {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String URL = "http://localhost:11434";

    private final System.Logger logger = System.getLogger(OllamaService.class.getName());

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();

    public OllamaResponse generate(OllamaRequest ollamaRequest) {
        switch (ollamaRequest) {
            case OllamaTextRequest textRequest -> {
                logger.log(System.Logger.Level.INFO, "Text request to: {0}", textRequest.model());
                logger.log(System.Logger.Level.INFO, "Prompt: {0}", textRequest.prompt());
            }
            case OllamaVisionRequest visionRequest -> {
                logger.log(System.Logger.Level.INFO, "Vision request to: {0}", visionRequest.model());
                logger.log(System.Logger.Level.INFO, "Size of uploaded image: {0}", visionRequest.images().getFirst().length());
                logger.log(System.Logger.Level.INFO, "Prompt: {0}", visionRequest.prompt());
            }
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL + "/api/generate"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(ollamaRequest)))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.log(System.Logger.Level.INFO, "Response: {0}", response.body());
            return gson.fromJson(response.body(), OllamaResponse.class);
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
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(ollamaTextRequest)))
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
        logger.log(System.Logger.Level.INFO,
                "Size of uploaded image: {0}", visionRequest.images().getFirst().length());
        logger.log(System.Logger.Level.INFO, "Prompt: {0}", visionRequest.prompt());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL + "/api/generate"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(visionRequest)))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), OllamaResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public OllamaChatResponse chat(OllamaChatRequest chatRequest) {
        String json = gson.toJson(chatRequest);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL + "/api/chat"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), OllamaChatResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
