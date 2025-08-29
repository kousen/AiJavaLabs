package com.kousenit;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static dev.langchain4j.model.LambdaStreamingResponseHandler.onPartialResponse;
import static dev.langchain4j.model.LambdaStreamingResponseHandler.onPartialResponseAndError;

public class OllamaLC4jTest {

    @Test
    @Tag(TestCategories.LOCAL)
    @Tag(TestCategories.QUICK)
    @Tag(TestCategories.DEMO)
    void testOllama() {
        ChatModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("gemma3")
                .build();
        System.out.println(model.chat("Why is the sky blue?"));
    }

    @Test
    @Tag(TestCategories.LOCAL)
    @Tag(TestCategories.SLOW)
    void testOllamaStreaming() {
        var gemma = OllamaStreamingChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("gemma3")
                .build();

        // Simple streaming with LambdaStreamingResponseHandler utility
        gemma.chat("Why is the sky blue?", onPartialResponse(System.out::print));
    }

    @Test
    @Tag(TestCategories.LOCAL)
    @Tag(TestCategories.DEMO)
    void testOllamaStreamingWithErrorHandling() {
        var gemma = OllamaStreamingChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("gemma3")
                .build();

        // Stream with both partial response and error handling
        gemma.chat("Tell me a joke", onPartialResponseAndError(
                System.out::print,
                Throwable::printStackTrace
        ));
    }
}
