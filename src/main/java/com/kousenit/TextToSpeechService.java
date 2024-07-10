package com.kousenit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TextToSpeechService {
    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
    public static final String RESOURCES_DIR = "src/main/resources";

    public Path generateMp3(String model, String input, String voice) {
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

        Path filePath = getFilePath();
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<Path> response =
                    client.send(request, HttpResponse.BodyHandlers.ofFile(filePath));
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getFilePath() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = String.format("audio_%s.png", timestamp);
        Path dir = Paths.get(RESOURCES_DIR);
        return dir.resolve(fileName);
    }
}
