package com.kousenit.demos;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * Quick demo showing basic chat interaction with OpenAI.
 * Good for live demonstrations - fast response, low cost.
 */
public class QuickChatDemo {
    public static void main(String[] args) {
        System.out.println("=== Quick Chat Demo with GPT-5-nano ===\n");
        
        ChatModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-5-nano")
                .logRequests(false)  // Set to true to show API calls
                .logResponses(false) // Set to true to show raw responses
                .build();
        
        String prompt = "Explain what LangChain4j is in 2-3 sentences.";
        System.out.println("Question: " + prompt);
        System.out.println("\nAnswer: " + model.chat(prompt));
        
        System.out.println("\n--- Demo Complete ---");
    }
}