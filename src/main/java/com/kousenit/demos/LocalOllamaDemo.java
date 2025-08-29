package com.kousenit.demos;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

/**
 * Demo using local Ollama models - completely free, no API costs.
 * Requires Ollama to be installed and running locally.
 * 
 * To set up:
 * 1. Install Ollama: https://ollama.ai
 * 2. Pull a model: ollama pull gemma3
 * 3. Ollama runs automatically on http://localhost:11434
 */
public class LocalOllamaDemo {
    public static void main(String[] args) {
        System.out.println("=== Local Ollama Demo (Free - No API Costs) ===\n");
        
        try {
            ChatModel model = OllamaChatModel.builder()
                    .baseUrl("http://localhost:11434")
                    .modelName("gemma3")
                    .build();
            
            String prompt = "What are the benefits of using local AI models?";
            System.out.println("Question: " + prompt);
            System.out.println("\nThinking locally...");
            
            String response = model.chat(prompt);
            System.out.println("\nAnswer: " + response);
            
        } catch (Exception e) {
            System.err.println("Error: Could not connect to Ollama.");
            System.err.println("Make sure Ollama is running locally on port 11434");
            System.err.println("Install from: https://ollama.ai");
        }
        
        System.out.println("\n--- Demo Complete ---");
    }
}