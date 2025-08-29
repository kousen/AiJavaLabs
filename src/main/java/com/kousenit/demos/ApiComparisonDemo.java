package com.kousenit.demos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Comparison demo showing:
 * 1. Chat Completions API via LangChain4j (what we use in the course)
 * 2. Chat Completions API via raw HTTP (what LC4j does internally)
 * 3. Responses API via raw HTTP (new, not yet in LC4j)
 * 
 * Key Teaching Points:
 * - Frameworks provide convenience and abstraction
 * - But understanding the underlying HTTP/JSON is valuable
 * - When new APIs emerge, you can use them immediately
 * - You're not blocked waiting for framework updates
 */
public class ApiComparisonDemo {
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public static void main(String[] args) {
        String prompt = "Why is the sky blue? Answer in one sentence.";
        System.out.printf("""
            === API Comparison Demo ===
            
            Question: %s
            %n""", prompt);
        
        // 1. Using LangChain4j (high-level abstraction)
        System.out.println("""
            1. Chat Completions via LangChain4j (Framework):
            -------------------------------------------------""");
        try {
            ChatModel model = OpenAiChatModel.builder()
                    .apiKey(API_KEY)
                    .modelName("gpt-5-nano")
                    .build();
            String response = model.chat(prompt);
            System.out.printf("""
                ✓ Response: %s
                   Pros: Clean API, provider-agnostic, type-safe
                   Cons: Depends on framework updates
                %n""", response);
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage() + "\n");
        }
        
        // 2. Using raw HTTP with Chat Completions API
        System.out.println("""
            2. Chat Completions via Raw HTTP (What LC4j does):
            ---------------------------------------------------""");
        try {
            chatCompletionsRaw(prompt);
            System.out.println("""
                   Pros: Direct control, no dependencies
                   Cons: More code, handle JSON manually
                """);
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage() + "\n");
        }
        
        // 3. Using raw HTTP with new Responses API
        System.out.println("""
            3. Responses API via Raw HTTP (New, not in LC4j):
            --------------------------------------------------""");
        try {
            responsesApiRaw(prompt);
            System.out.println("""
                   Pros: Latest features immediately available
                   Cons: No framework support yet
                """);
        } catch (Exception e) {
            System.out.printf("""
                ✗ Error: %s
                   (Responses API may not be available yet)
                %n""", e.getMessage());
        }
        
        System.out.println("""
            
            === Key Lessons ===
            1. Frameworks (LC4j) make common tasks easy
            2. Understanding HTTP/JSON gives you flexibility
            3. You can adopt new APIs before frameworks catch up
            4. Both skills are valuable for different situations
            """);
    }
    
    private static void chatCompletionsRaw(String prompt) throws IOException, InterruptedException {
        var requestBody = Map.of(
            "model", "gpt-5-nano",
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            ),
            "temperature", 0.7
        );
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            var json = gson.fromJson(response.body(), Map.class);
            var choices = (List<Map>) json.get("choices");
            var message = (Map) choices.get(0).get("message");
            System.out.println("✓ Response: " + message.get("content"));
        } else {
            System.out.println("✗ Status: " + response.statusCode());
        }
    }
    
    private static void responsesApiRaw(String prompt) throws IOException, InterruptedException {
        var requestBody = Map.of(
            "model", "gpt-5-nano",
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            )
            // Note: Simpler API, server manages conversation state
        );
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/responses"))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            var json = gson.fromJson(response.body(), Map.class);
            System.out.println("✓ Response: " + json.get("content"));
        } else {
            System.out.println("✗ Status: " + response.statusCode());
        }
    }
}