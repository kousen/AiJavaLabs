package com.kousenit.demos;

import static dev.langchain4j.model.LambdaStreamingResponseHandler.onPartialResponseAndErrorBlocking;
import static dev.langchain4j.model.LambdaStreamingResponseHandler.onPartialResponseBlocking;

import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * Demonstrates the clean streaming API using LambdaStreamingResponseHandler utilities.
 * Shows how to use lambda expressions instead of anonymous inner classes.
 *
 * Streaming is asynchronous: chat(...) returns before the tokens arrive. In a
 * standalone main that would exit before anything prints, so this demo uses the
 * *Blocking variants, which wait for the stream to complete.
 */
public class StreamingDemo {

    public static void main(String[] args) {
        System.out.println(
                """
            === Streaming Demo with LambdaStreamingResponseHandler ===

            This demonstrates the cleaner streaming pattern available in LangChain4j.
            """);

        // Example 1: Simple streaming with just token handling
        System.out.println(
                """

            1. Simple Streaming (Local Ollama):
            -----------------------------------""");
        try {
            var ollama = OllamaStreamingChatModel.builder()
                    .baseUrl("http://localhost:11434")
                    .modelName("gemma4")
                    .build();

            // Clean one-liner for simple streaming; blocks until the response is complete
            onPartialResponseBlocking(ollama, "Tell me a haiku about Java", System.out::print);
            System.out.println("\n");
        } catch (Exception e) {
            System.out.println("Ollama not available - is it running?");
        }

        // Example 2: Streaming with OpenAI including error handling
        System.out.println(
                """

            2. Streaming with Full Control (OpenAI):
            ----------------------------------------""");
        try {
            var openai = OpenAiStreamingChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName("gpt-4o-mini")
                    .build();

            // Handle both partial responses and errors
            onPartialResponseAndErrorBlocking(
                    openai,
                    "Why is the sky blue? Answer in exactly one sentence.",
                    System.out::print, // Handle each token
                    error -> System.err.println("\nError: " + error.getMessage())); // Handle errors
            System.out.println();
        } catch (Exception e) {
            System.out.println("OpenAI not available - check OPENAI_API_KEY");
        }

        System.out.println(
                """

            === Key Benefits ===
            1. No anonymous inner classes needed
            2. Clean, functional style with lambdas
            3. Choose simple (onPartialResponse) or with error handling (onPartialResponseAndError)
            4. Less boilerplate code
            """);
    }
}
