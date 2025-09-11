package com.kousenit.demos;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static com.kousenit.demos.LMStudioRecords.*;

public class LMStudioService {

    //record TextRequest(String model, String prompt, boolean stream) {}

    public record ModelListResponse(
            List<Model> data,
            String object
    ) {}

    public record Model(
            String id,
            String object,
            String type,
            String publisher,
            String arch,
            String compatibility_type,
            String quantization,
            String state,
            int max_context_length,
            Integer loaded_context_length,
            List<String> capabilities
    ) {}


    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)  // Required for LM Studio (!)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String URL = "http://localhost:1234";

    private final System.Logger logger = System.getLogger(LMStudioService.class.getName());

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();

    public List<Model> getModels() {
        logger.log(System.Logger.Level.INFO,
                "Requesting models from: {0}", URL + "/v1/models");
        var request = HttpRequest.newBuilder()
                .uri(URI.create(URL + "/v1/models"))
                .header("Accept", "application/json")
                .header("User-Agent", "Java-HttpClient")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.log(System.Logger.Level.INFO, "Response: {0}", response.body());
            return gson.fromJson(response.body(), ModelListResponse.class).data();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public ChatCompletionResponse chat(ChatRequest chatRequest) {

        logger.log(System.Logger.Level.INFO, "Request: {0}", gson.toJson(chatRequest));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL + "/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(chatRequest)))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.log(System.Logger.Level.INFO, "Response: {0}", response.body());
            return gson.fromJson(response.body(), ChatCompletionResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
