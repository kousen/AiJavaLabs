package com.kousenit;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.output.Response;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

public class OllamaLC4jTest {

    @Test
    void testOllama() {
        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("gemma")
                .build();
        System.out.println(model.generate("Why is the sky blue?"));
    }

    @Test
    void testOllamaStreaming() {
        var gemma = OllamaStreamingChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("gemma")
                .build();
        CompletableFuture<Response<AiMessage>> futureResponse = new CompletableFuture<>();
        gemma.generate("Why is the sky blue?", new StreamingResponseHandler<>() {

            @Override
            public void onNext(String token) {
                System.out.print(token);
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
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
