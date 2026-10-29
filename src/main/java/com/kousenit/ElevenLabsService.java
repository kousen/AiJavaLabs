package com.kousenit;

import static com.kousenit.ElevenLabsRecords.*;
import static com.kousenit.HttpSupport.requireSuccess;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

/**
 * ElevenLabs text-to-speech over raw HTTP. ElevenLabs ships SDKs for Python and JavaScript
 * but not Java, which changes nothing: the API is a GET for voices and a POST that returns
 * audio bytes, the same shape as the OpenAI text-to-speech lab. Only the details differ:
 * the key travels in an xi-api-key header, the voice is part of the URL, and the model id
 * goes in the body.
 */
public class ElevenLabsService {
    private static final String API_KEY = System.getenv("ELEVENLABS_API_KEY");
    private static final String BASE_URL = "https://api.elevenlabs.io/v1";
    public static final String DEFAULT_MODEL = "eleven_multilingual_v2";

    private static final HttpClient client = HttpClient.newHttpClient();

    private final ObjectMapper mapper = JsonMapper.builder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();

    public List<Voice> listVoices() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/voices"))
                .header("xi-api-key", API_KEY)
                .header("Accept", "application/json")
                .build();
        try {
            HttpResponse<String> response = requireSuccess(client.send(request, HttpResponse.BodyHandlers.ofString()));
            return mapper.readValue(response.body(), VoiceList.class).voices();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /** Finds a voice by its display name, such as "George" or "Sarah", among the account's voices. */
    public Voice voiceNamed(String name) {
        return listVoices().stream()
                .filter(voice -> voice.name().toLowerCase().startsWith(name.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No voice named " + name));
    }

    public Path textToSpeech(String voiceId, String text) {
        return textToSpeech(voiceId, text, DEFAULT_MODEL);
    }

    public Path textToSpeech(String voiceId, String text, String modelId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/text-to-speech/" + voiceId))
                .header("xi-api-key", API_KEY)
                .header("Content-Type", "application/json")
                .header("Accept", "audio/mpeg")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(new SpeechRequest(text, modelId))))
                .build();
        Path filePath = outputPath();
        try {
            HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(filePath));
            return HttpSupport.requireSuccessFile(response).body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path outputPath() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return Paths.get(TextToSpeechService.RESOURCES_DIR).resolve("elevenlabs_%s.mp3".formatted(timestamp));
    }
}
