package com.kousenit.demos;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * Demo comparing responses from multiple AI providers.
 * Shows how LangChain4j provides a unified interface across different models.
 * The cloud models all go through OpenRouter, so one API key reaches every
 * provider and only the model id changes.
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

        // OpenAI via OpenRouter
        try {
            System.out.print("OpenAI (openai/gpt-5-nano via OpenRouter): ");
            System.out.println(openRouter("openai/gpt-5-nano").chat(question));
        } catch (Exception e) {
            System.out.println("Not available - check OPENROUTER_API_KEY");
        }

        // Google Gemini via OpenRouter
        try {
            System.out.print("Google (google/gemini-3-flash-preview via OpenRouter): ");
            System.out.println(openRouter("google/gemini-3-flash-preview").chat(question));
        } catch (Exception e) {
            System.out.println("Not available - check OPENROUTER_API_KEY");
        }

        // Local Ollama
        try {
            System.out.print("Ollama (gemma4 - local): ");
            ChatModel ollama = OllamaChatModel.builder()
                    .baseUrl("http://localhost:11434")
                    .modelName("gemma4")
                    .build();
            System.out.println(ollama.chat(question));
        } catch (Exception e) {
            System.out.println("Not available - is Ollama running?");
        }

        System.out.println(
                """

            --- Demo Complete ---
            Notice: Same interface (ChatModel) for all providers!
            """);
    }
}
