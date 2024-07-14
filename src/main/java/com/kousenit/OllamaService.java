package com.kousenit;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

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
        logger.log(System.Logger.Level.INFO,
                "Generating streaming response with request: {0}", ollamaTextRequest);
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

    public String generateStreamingResponse(OllamaTextRequest ollamaRequest) {
        try (var client = HttpClient.newHttpClient()) {
            var requestBody = gson.toJson(ollamaRequest);
            var httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(URL + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            return handleStreamingRequest(client, httpRequest);
        }
    }

    private String handleStreamingRequest(HttpClient client, HttpRequest request) {
        try (var publisher = new SubmissionPublisher<String>()) {
            var subscriber = new OllamaSubscriber();
            publisher.subscribe(subscriber);

            var responseFuture = client.sendAsync(
                            request,
                            HttpResponse.BodyHandlers.fromLineSubscriber(subscriber))
                    .thenApply(HttpResponse::body);
            responseFuture.join();

            return subscriber.getAccumulatedResponse();
        }
    }
    private class OllamaSubscriber implements Flow.Subscriber<String> {
        private Flow.Subscription subscription;
        private final StringBuilder accumulatedResponse = new StringBuilder();

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(String item) {
            try {
                var response = gson.fromJson(item, OllamaResponse.class);
                System.out.print(response.response());
                accumulatedResponse.append(response.response());

                if (response.done()) {
                    // You can log or process the final response metadata here if needed
                    System.out.println();
                    System.out.println("Final response metadata: " + item);
                } else {
                    subscription.request(1);
                }
            } catch (Exception e) {
                System.err.println("Error processing JSON: " + e.getMessage());
            }
        }

        @Override
        public void onError(Throwable throwable) {
            System.err.println("Error occurred: " + throwable.getMessage());
        }

        @Override
        public void onComplete() {
            // Streaming completed
        }

        public String getAccumulatedResponse() {
            return accumulatedResponse.toString();
        }
    }

}
