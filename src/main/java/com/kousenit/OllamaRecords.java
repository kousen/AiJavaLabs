package com.kousenit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class OllamaRecords {

    public sealed interface OllamaRequest
            permits OllamaTextRequest, OllamaVisionRequest { }

    public record OllamaTextRequest(
            String model,
            String prompt,
            boolean stream)
    implements OllamaRequest { }

    public record OllamaVisionRequest(
            String model,
            String prompt,
            boolean stream,
            List<String> images)
    implements OllamaRequest {

        public OllamaVisionRequest {
            images = images.stream()
                    .map(this::encodeImage)
                    .collect(Collectors.toList());
        }

        private String encodeImage(String path) {
            try {
                byte[] imageBytes = Files.readAllBytes(Paths.get(path));
                return Base64.getEncoder().encodeToString(imageBytes);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public record OllamaResponse(
            String model,
            String createdAt,
            String response,
            boolean done) { }

    public record OllamaMessage(
            String role,
            String content) {

        public OllamaMessage {
            if (!List.of("user", "assistant", "system").contains(role)) {
                throw new IllegalArgumentException("Invalid role: " + role);
            }
        }
    }

    public record OllamaChatRequest(
            String model,
            List<OllamaMessage> messages,
            boolean stream) {}

    public record OllamaChatResponse(
            String model,
            String createdAt,
            OllamaMessage message,
            boolean done) {}
}
