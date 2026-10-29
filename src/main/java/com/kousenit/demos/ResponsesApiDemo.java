package com.kousenit.demos;

import static com.kousenit.HttpSupport.requireSuccess;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Demo of OpenAI's Responses API using raw HTTP calls and Jackson's tree model.
 * <p>
 * This demonstrates why understanding HTTP clients and JSON parsing is valuable:
 * - API features and response shapes change faster than courseware
 * - You're not blocked waiting for library updates
 * - You understand what frameworks do under the hood
 * <p>
 * The Responses API offers server-side conversation management, built-in tools such as
 * web search, and a single endpoint. Its output is an array of items of different types
 * (reasoning, web_search_call, message, ...), so this demo shows both ways to read a
 * JsonNode tree: a JSON Pointer for values at a known location, and a walk over an array
 * whose contents vary from one response to the next.
 * <p>
 * LangChain4j also exposes Responses API support through OpenAI-specific abstractions.
 * This demo keeps the raw HTTP version because it is useful for debugging, learning the
 * endpoint shape, and adopting features directly.
 */
public class ResponsesApiDemo {
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String RESPONSES_URL = "https://api.openai.com/v1/responses";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = JsonMapper.builder().build();

    public static void main(String[] args) {
        System.out.println(
                """
            === OpenAI Responses API Demo (Raw HTTP) ===

            This uses the Responses API, not Chat Completions.
            Raw HTTP shows the endpoint shape without framework abstraction.
            """);

        try {
            simpleResponse();
            System.out.println();
            responseWithWebSearch();
        } catch (Exception e) {
            System.err.printf(
                    """
                Error: %s

                Note: The endpoint or request/response format may have changed.
                %n""",
                    e.getMessage());
        }

        System.out.println(
                """

            --- Demo Complete ---
            Key Lesson: Understanding HTTP/JSON lets you use new APIs immediately!
            """);
    }

    private static void simpleResponse() throws IOException, InterruptedException {
        System.out.println("1. Simple Response (no tools):");
        System.out.println("------------------------------");

        // Key difference from Chat Completions: "input" instead of "messages"
        var requestBody = Map.of(
                "model", "gpt-5-nano",
                "input", "What's the main difference between the Responses API and Chat Completions API?");

        JsonNode root = post(requestBody);

        // JSON Pointer (RFC 6901) for values whose location never changes
        System.out.println("Response id: " + root.at("/id").asString());
        System.out.println("Output tokens: " + root.at("/usage/output_tokens").asInt());

        System.out.println("Response: " + extractMessageText(root));
    }

    private static void responseWithWebSearch() throws IOException, InterruptedException {
        System.out.println("2. Response with Web Search Tool:");
        System.out.println("----------------------------------");

        // The API runs the web search itself; the output array gains a web_search_call item
        var requestBody = Map.of(
                "model", "gpt-5-nano",
                "input", "What is the most recent LangChain4j release? Search for current information.",
                "tools", List.of(Map.of("type", "web_search")));

        JsonNode root = post(requestBody);

        // The walk reports what kinds of items came back, which a fixed pointer could not
        for (JsonNode item : root.path("output")) {
            System.out.println("  output item type: " + item.path("type").asString());
        }
        System.out.println("Response with web search: " + extractMessageText(root));
    }

    private static JsonNode post(Map<String, ?> requestBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RESPONSES_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = requireSuccess(client.send(request, HttpResponse.BodyHandlers.ofString()));
        return mapper.readTree(response.body());
    }

    /**
     * The output array holds items of several types, and the message is rarely first
     * (reasoning models put a reasoning item ahead of it), so a pointer such as
     * /output/0/content/0/text is the wrong tool. Walk the array, find the message,
     * then use a pointer relative to it.
     */
    private static String extractMessageText(JsonNode root) {
        for (JsonNode item : root.path("output")) {
            if ("message".equals(item.path("type").asString())) {
                return item.at("/content/0/text").asString("(no text)");
            }
        }
        return "(no message item in output)";
    }
}
