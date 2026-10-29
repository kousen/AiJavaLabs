package com.kousenit.demos;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Demo comparing responses from multiple AI providers.
 * Shows how LangChain4j provides a unified interface across different models.
 * The cloud models all go through OpenRouter, so one API key reaches every
 * provider and only the model id changes.
 *
 * Part two asks every model at once. Each chat() call is a blocking HTTP request
 * that spends almost all of its time waiting on the network, which is exactly the
 * work virtual threads were designed for: one thread per call, no pool to size.
 */
public class MultiModelDemo {
    private static final String OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1";

    private static ChatModel openRouter(String modelId) {
        return OpenAiChatModel.builder()
                .baseUrl(OPENROUTER_BASE_URL)
                .apiKey(System.getenv("OPENROUTER_API_KEY"))
                .modelName(modelId)
                .build();
    }

    public static void main(String[] args) {
        String question = "What is the capital of France? Answer in exactly one word.";
        System.out.printf(
                """
            === Multi-Model Comparison Demo ===

            Question: %s
            %n""",
                question);

        // Same ChatModel interface whether the request goes to OpenRouter or to local Ollama
        Map<String, ChatModel> models = new LinkedHashMap<>();
        models.put("openai/gpt-5-nano (OpenRouter)", openRouter("openai/gpt-5-nano"));
        models.put("google/gemini-3-flash-preview (OpenRouter)", openRouter("google/gemini-3-flash-preview"));
        models.put(
                "gemma4 (Ollama, local)",
                OllamaChatModel.builder()
                        .baseUrl("http://localhost:11434")
                        .modelName("gemma4")
                        .build());

        System.out.println("--- One at a time ---");
        Instant start = Instant.now();
        models.forEach((name, model) -> System.out.printf("%-42s %s%n", name + ":", ask(model, question)));
        System.out.printf(
                "Sequential: %d ms%n%n", Duration.between(start, Instant.now()).toMillis());

        System.out.println("--- All at once, one virtual thread per call ---");
        start = Instant.now();
        Map<String, Future<String>> answers = new LinkedHashMap<>();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            models.forEach((name, model) -> answers.put(name, executor.submit(() -> ask(model, question))));
        } // close() waits for every task, so each future below is already complete
        answers.forEach((name, future) -> System.out.printf("%-42s %s%n", name + ":", future.resultNow()));
        System.out.printf(
                "Concurrent: %d ms%n", Duration.between(start, Instant.now()).toMillis());

        System.out.println(
                """

            --- Demo Complete ---
            Same interface for every provider, and the concurrent version is the same code
            whether you ask 3 models or 300.
            """);
    }

    private static String ask(ChatModel model, String question) {
        try {
            return model.chat(question).trim();
        } catch (Exception e) {
            return "Not available: " + e.getMessage(); // missing OPENROUTER_API_KEY, Ollama not running, ...
        }
    }
}
