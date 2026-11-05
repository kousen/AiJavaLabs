package com.kousenit;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

/**
 * Request and response shapes for Ollama's HTTP API.
 *
 * <p>The chat endpoint takes a list of messages and is the same shape every other vendor
 * uses. The generate endpoint takes a single prompt and is Ollama's own; it is kept here
 * as an aside.
 */
public class OllamaRecords {

    /** One turn in a conversation. Images ride along on the message as Base64 strings. */
    public record OllamaMessage(
            String role, String content, @JsonInclude(JsonInclude.Include.NON_NULL) List<String> images) {

        public OllamaMessage {
            if (!List.of("user", "assistant", "system").contains(role)) {
                throw new IllegalArgumentException("Invalid role: " + role);
            }
        }

        public OllamaMessage(String role, String content) {
            this(role, content, null);
        }

        public static OllamaMessage user(String content) {
            return new OllamaMessage("user", content);
        }

        public static OllamaMessage assistant(String content) {
            return new OllamaMessage("assistant", content);
        }

        /** A user message with one local image attached, encoded the way the API wants it. */
        public static OllamaMessage user(String content, Path image) {
            try {
                String encoded = Base64.getEncoder().encodeToString(Files.readAllBytes(image));
                return new OllamaMessage("user", content, List.of(encoded));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public record OllamaChatRequest(String model, List<OllamaMessage> messages, boolean stream) {}

    public record OllamaChatResponse(String model, String createdAt, OllamaMessage message, boolean done) {}

    // The generate endpoint: one prompt in, one completion out. Ollama-only; nothing else in the course uses it.
    public record OllamaGenerateRequest(String model, String prompt, boolean stream) {}

    public record OllamaGenerateResponse(String model, String createdAt, String response, boolean done) {}
}
