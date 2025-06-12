package com.kousenit;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

public class OllamaLC4jTest {

    @Test
    void testOllama() {
        ChatModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("gemma3")
                .build();
        System.out.println(model.chat("Why is the sky blue?"));
    }

    @Test
    void testOllamaStreaming() {
        var gemma = OllamaStreamingChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("gemma3")
                .build();

        // Use CompletableFuture to handle the asynchronous response
        var futureResponse = new CompletableFuture<>();

        gemma.chat("Why is the sky blue?", new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String token) {
                System.out.print(token);
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                futureResponse.complete(response);
            }

            @Override
            public void onError(Throwable error) {
                futureResponse.completeExceptionally(error);
            }
        });
        futureResponse.join();
    }
}
