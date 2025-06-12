package com.kousenit;

import static com.kousenit.OllamaRecords.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OllamaServiceTest {
    private final OllamaService service = new OllamaService();

    @Test
    void generate_with_text_request() {
        var ollamaRequest = new OllamaTextRequest("gemma3", "Why is the sky blue?", false);
        OllamaResponse ollamaResponse = service.generate(ollamaRequest);
        System.out.println(ollamaResponse);
        String answer = ollamaResponse.response();
        System.out.println(answer);
        assertTrue(answer.contains("scattering"));
        assertNotNull(ollamaResponse.createdAt());
    }

    @Test
    void generate_with_vision_request() {
        var request = new OllamaVisionRequest(
                "moondream",
                """
                Generate a text description of this image
                suitable for accessibility in HTML.
                """,
                false,
                List.of("src/main/resources/cats_playing_cards.png"));
        OllamaResponse response = service.generateVision(request);
        assertNotNull(response);
        System.out.println(response);
    }

    @Test
    void generate_with_model_and_name() {
        var ollamaResponse = service.generate("gemma3", "Why is the sky blue?");
        String answer = ollamaResponse.response();
        System.out.println(answer);
        assertTrue(answer.contains("scattering"));
    }

    @Test
    public void streaming_generate_request() {
        var request = new OllamaTextRequest("gemma3", "Why is the sky blue?", true);
        String response = service.generateStreaming(request);
        System.out.println(response);
    }


    @Test
    void test_chat() {
        var request = new OllamaChatRequest(
                "gemma3",
                List.of(
                        new OllamaMessage("user", "why is the sky blue?"),
                        new OllamaMessage("assistant", "due to rayleigh scattering."),
                        new OllamaMessage("user", "how is that different than mie scattering?")),
                false);
        OllamaChatResponse response = service.chat(request);
        assertNotNull(response);
        System.out.println(response);
    }

    @Test
    void testStreamingRequest() {
        var request = new OllamaTextRequest("gemma3", "Why is the sky blue?", true);
        var response = service.generateStreamingResponse(request);
        assertThat(response).isNotEmpty().containsIgnoringCase("sky").containsIgnoringCase("blue");
    }
}
