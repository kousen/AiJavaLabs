package com.kousenit.demos;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * Quick demo showing basic chat interaction through OpenRouter.
 * OpenRouter exposes an OpenAI-compatible API, so LangChain4j's OpenAiChatModel
 * works with just a different base URL, API key, and provider-prefixed model id.
 * Good for live demonstrations - fast response, low cost.
 */
public class QuickChatDemo {
    private static final String OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1";

    public static void main(String[] args) {
        System.out.println("=== Quick Chat Demo with GPT-5-nano via OpenRouter ===\n");

        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(OPENROUTER_BASE_URL)
                .apiKey(System.getenv("OPENROUTER_API_KEY"))
                .modelName("openai/gpt-5-nano")
                .logRequests(false) // Set to true to show API calls
                .logResponses(false) // Set to true to show raw responses
                .build();

        String prompt = "Explain what LangChain4j is in 2-3 sentences.";
        System.out.println("Question: " + prompt);
        System.out.println("\nAnswer: " + model.chat(prompt));

        System.out.println("\n--- Demo Complete ---");
    }
}
