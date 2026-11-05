package com.kousenit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TextToSpeechService {
    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
    // Generated audio goes under build/: gitignored, kept out of the jar, removed by `gradlew clean`
    public static final Path AUDIO_DIR = Paths.get("build", "audio");

    // HttpClient is thread-safe and designed to be reused for multiple requests
    // Using a single instance improves performance through connection pooling
    private static final HttpClient client = HttpClient.newHttpClient();

    public Path generateMp3(String model, String input, String voice) {
        String payload =
                """
                {
                    "model": "%s",
                    "input": "%s",
                    "voice": "%s"
                }
                """
                        .formatted(model, input.replaceAll("\\s+", " ").trim(), voice);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/audio/speech"))
                .header("Authorization", "Bearer %s".formatted(OPENAI_API_KEY))
                .header("Content-Type", "application/json")
                .header("Accept", "audio/mpeg")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        Path filePath = getFilePath();
        try {
            HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(filePath));
            // The file handler saves an error body as if it were audio; the helper reads it back and removes the file
            return HttpSupport.requireSuccessFile(response).body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getFilePath() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = String.format("audio_%s.mp3", timestamp);
        return audioDir().resolve(fileName);
    }

    /** The output directory, created on first use (BodyHandlers.ofFile needs it to exist). */
    public static Path audioDir() {
        try {
            return Files.createDirectories(AUDIO_DIR);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
