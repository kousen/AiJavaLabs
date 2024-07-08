package com.kousenit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TextToSpeechService {
    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");

    public byte[] generateMp3(String model, String input, String voice) {
        String payload = """
                {
                    "model": "%s",
                    "input": "%s",
                    "voice": "%s"
                }
                """.formatted(model, input.replaceAll("\\s+", " ")
                .trim(), voice);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/audio/speech"))
                .header("Authorization", "Bearer %s".formatted(OPENAI_API_KEY))
                .header("Content-Type", "application/json")
                .header("Accept", "audio/mpeg")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<byte[]> response =
                    client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
