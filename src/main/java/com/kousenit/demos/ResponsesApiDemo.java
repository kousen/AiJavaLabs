package com.kousenit.demos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Demo of OpenAI's new Responses API (March 2025) using raw HTTP calls.
 * <p>
 * This demonstrates why understanding HTTP clients and JSON parsing is valuable:
 * - New APIs emerge faster than framework support
 * - You're not blocked waiting for library updates
 * - You understand what frameworks do under the hood
 * <p>
 * The Responses API offers:
 * - Server-side conversation management
 * - Built-in tools (web search, code execution)
 * - Single endpoint design
 * <p>
 * Note: LangChain4j doesn't support this yet (as of Aug 2025),
 * which is why we're using raw HTTP calls here.
 */
public class ResponsesApiDemo {
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String RESPONSES_URL = "https://api.openai.com/v1/responses";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public static void main(String[] args) {
        System.out.println("""
            === OpenAI Responses API Demo (Raw HTTP) ===
            
            This uses the NEW Responses API, not Chat Completions.
            Frameworks like LangChain4j don't support this yet.
            """);
        
        try {
            // Simple prompt without tools
            simpleResponse();
            
            // Example with web search tool
            System.out.println(); // Add spacing between demos
            responseWithWebSearch();
            
        } catch (Exception e) {
            System.err.printf("""
                Error: %s
                
                Note: The Responses API may not be available yet
                or the endpoint/format may have changed.
                %n""", e.getMessage());
        }
        
        System.out.println("""
            
            --- Demo Complete ---
            Key Lesson: Understanding HTTP/JSON lets you use new APIs immediately!
            """);
    }
    
    private static void simpleResponse() throws IOException, InterruptedException {
        System.out.println("1. Simple Response (no tools):");
        System.out.println("------------------------------");
        
        // Build request body for Responses API
        // Key difference: uses "input" instead of "messages"
        var requestBody = Map.of(
            "model", "gpt-5-nano",
            "input", "What's the main difference between the Responses API and Chat Completions API?"
            // Note: No need to maintain conversation history client-side
        );
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RESPONSES_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();
        
        HttpResponse<String> response = client.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            // Alternative: Use JsonElement for cleaner navigation (similar to Jackson's JsonNode)
            JsonElement root = JsonParser.parseString(response.body());
            String responseText = extractMessageText(root);
            if (responseText != null) {
                System.out.println("Response: " + responseText);
            }
        } else {
            System.out.println("Status: " + response.statusCode());
            System.out.println("Body: " + response.body());
        }
    }
    
    private static void responseWithWebSearch() throws IOException, InterruptedException {
        System.out.println("2. Response with Web Search Tool:");
        System.out.println("----------------------------------");
        
        // Build request with web search tool enabled
        // This demonstrates the API's ability to search the web for current information
        var requestBody = Map.of(
            "model", "gpt-5-nano",
            "input", "What are the latest features in LangChain4j 1.4.0? Search for current information.",
            "tools", List.of(
                Map.of("type", "web_search")
            )
        );
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RESPONSES_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();
        
        HttpResponse<String> response = client.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            // Use JsonElement for cleaner navigation
            JsonElement root = JsonParser.parseString(response.body());
            String responseText = extractMessageText(root);
            if (responseText != null) {
                System.out.println("Response with web search: " + responseText);
            }
            
            // The API handles web search internally and returns the result
            // No need for separate tool calls like Chat Completions API
        } else {
            System.out.println("Status: " + response.statusCode());
            System.out.println("Body: " + response.body());
        }
    }
    
    /**
     * Helper method to extract message text from the Responses API JSON structure.
     * This is similar to using Jackson's JsonNode.at() but with Gson's JsonElement.
     * 
     * @param root The root JsonElement from the API response
     * @return The extracted text content, or null if not found
     */
    private static String extractMessageText(JsonElement root) {
        if (!root.isJsonObject()) return null;
        
        JsonObject jsonObject = root.getAsJsonObject();
        JsonArray outputs = jsonObject.getAsJsonArray("output");
        
        if (outputs != null) {
            // Iterate through output array to find message type
            for (JsonElement outputElement : outputs) {
                JsonObject outputItem = outputElement.getAsJsonObject();
                String type = outputItem.has("type") ? outputItem.get("type").getAsString() : null;
                
                if ("message".equals(type)) {
                    JsonArray content = outputItem.getAsJsonArray("content");
                    if (content != null && !content.isEmpty()) {
                        JsonObject textContent = content.get(0).getAsJsonObject();
                        if (textContent.has("text")) {
                            return textContent.get("text").getAsString();
                        }
                    }
                }
            }
        }
        return null;
    }
}