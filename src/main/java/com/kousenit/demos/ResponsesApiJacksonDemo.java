package com.kousenit.demos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Demo of OpenAI's Responses API using Jackson for JSON parsing.
 * <p>
 * This version demonstrates the elegance of Jackson's JsonNode API,
 * particularly the `at()` method for navigating JSON structures using
 * JSON Pointer notation (RFC 6901).
 * <p>
 * Compare this with ResponsesApiDemo which uses Gson - both achieve
 * the same result but with different JSON parsing approaches.
 */
public class ResponsesApiJacksonDemo {
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String RESPONSES_URL = "https://api.openai.com/v1/responses";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static void main(String[] args) {
        System.out.println("""
            === OpenAI Responses API Demo (Jackson Version) ===
            
            Using Jackson's JsonNode.at() for elegant JSON navigation.
            Compare with ResponsesApiDemo.java for the Gson approach.
            """);
        
        try {
            // Simple prompt without tools
            simpleResponse();
            
            // Example with web search tool
            System.out.println();
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
            Jackson's at() method with JSON Pointer makes navigation clean!
            Example: root.at("/output/0/content/0/text")
            """);
    }
    
    private static void simpleResponse() throws IOException, InterruptedException {
        System.out.println("1. Simple Response (no tools):");
        System.out.println("------------------------------");
        
        // Build request body
        var requestBody = Map.of(
            "model", "gpt-5-nano",
            "input", "What's the main advantage of using Jackson's JsonNode over Map<String, Object>?"
        );
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RESPONSES_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();
        
        HttpResponse<String> response = client.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode root = mapper.readTree(response.body());
            
            // Elegant navigation using JSON Pointer with at()
            // This tries to find the text in the first message content
            String text = extractTextFromResponse(root);
            
            if (text != null) {
                System.out.println("Response: " + text);
            } else {
                System.out.println("Could not extract text from response");
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
        var requestBody = Map.of(
            "model", "gpt-5-nano",
            "input", "What are the latest features in Jackson 2.18? Search for current information.",
            "tools", new Object[]{Map.of("type", "web_search")}
        );
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RESPONSES_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();
        
        HttpResponse<String> response = client.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode root = mapper.readTree(response.body());
            
            // Extract text using our helper method
            String text = extractTextFromResponse(root);
            
            if (text != null) {
                System.out.println("Response with web search: " + text);
            } else {
                System.out.println("Could not extract text from response");
            }
        } else {
            System.out.println("Status: " + response.statusCode());
            System.out.println("Body: " + response.body());
        }
    }
    
    /**
     * Extract text from the Responses API JSON structure.
     * <p>
     * The Responses API returns an array of outputs. We need to find
     * the one with type="message" and extract its text content.
     * <p>
     * This method demonstrates two approaches:
     * 1. Direct JSON Pointer navigation (fast but assumes structure)
     * 2. Iterating through array to find correct type (safer)
     * 
     * @param root The root JsonNode from the API response
     * @return The extracted text content, or null if not found
     */
    private static String extractTextFromResponse(JsonNode root) {
        // First, try the simple JSON Pointer approach
        // This assumes the message is in a predictable location
        JsonNode textNode = root.at("/output/0/content/0/text");
        if (!textNode.isMissingNode()) {
            return textNode.asText();
        }
        
        // If that didn't work, iterate through outputs to find message type
        JsonNode outputs = root.get("output");
        if (outputs != null && outputs.isArray()) {
            for (JsonNode output : outputs) {
                String type = output.path("type").asText();
                if ("message".equals(type)) {
                    // Found the message, now extract text from content array
                    JsonNode content = output.path("content");
                    if (content.isArray() && !content.isEmpty()) {
                        return content.get(0).path("text").asText();
                    }
                }
            }
        }
        
        return null;
    }
}