package com.kousenit.demos;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * Demo comparing responses from multiple AI providers.
 * Shows how LangChain4j provides a unified interface across different models.
 */
public class MultiModelDemo {
    public static void main(String[] args) {
        String question = "What is the capital of France? Answer in exactly one word.";
        System.out.printf("""
            === Multi-Model Comparison Demo ===
            
            Question: %s
            %n""", question);
        
        // OpenAI
        try {
            System.out.print("OpenAI (gpt-5-nano): ");
            ChatModel openai = OpenAiChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName("gpt-5-nano")
                    .build();
            System.out.println(openai.chat(question));
        } catch (Exception e) {
            System.out.println("Not available - check OPENAI_API_KEY");
        }
        
        // Google Gemini
        try {
            System.out.print("Google (gemini-2.0-flash-exp): ");
            ChatModel gemini = GoogleAiGeminiChatModel.builder()
                    .apiKey(System.getenv("GOOGLEAI_API_KEY"))
                    .modelName("gemini-2.0-flash-exp")
                    .build();
            System.out.println(gemini.chat(question));
        } catch (Exception e) {
            System.out.println("Not available - check GOOGLEAI_API_KEY");
        }
        
        // Local Ollama
        try {
            System.out.print("Ollama (gemma3 - local): ");
            ChatModel ollama = OllamaChatModel.builder()
                    .baseUrl("http://localhost:11434")
                    .modelName("gemma3")
                    .build();
            System.out.println(ollama.chat(question));
        } catch (Exception e) {
            System.out.println("Not available - is Ollama running?");
        }
        
        System.out.println("""
            
            --- Demo Complete ---
            Notice: Same interface (ChatModel) for all providers!
            """);
    }
}