package com.kousenit.demos;

import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import static dev.langchain4j.model.LambdaStreamingResponseHandler.onPartialResponse;
import static dev.langchain4j.model.LambdaStreamingResponseHandler.onPartialResponseAndError;

/**
 * Demonstrates the clean streaming API using LambdaStreamingResponseHandler utilities.
 * Shows how to use lambda expressions instead of anonymous inner classes.
 */
public class StreamingDemo {
    
    public static void main(String[] args) {
        System.out.println("""
            === Streaming Demo with LambdaStreamingResponseHandler ===
            
            This demonstrates the cleaner streaming pattern available in LangChain4j.
            """);
        
        // Example 1: Simple streaming with just token handling
        System.out.println("""
            
            1. Simple Streaming (Local Ollama):
            -----------------------------------""");
        try {
            var ollama = OllamaStreamingChatModel.builder()
                    .baseUrl("http://localhost:11434")
                    .modelName("gemma3")
                    .build();
            
            // Clean one-liner for simple streaming
            ollama.chat("Tell me a haiku about Java", onPartialResponse(System.out::print));
            System.out.println("\n");
        } catch (Exception e) {
            System.out.println("Ollama not available - is it running?");
        }
        
        // Example 2: Streaming with OpenAI including error handling
        System.out.println("""
            
            2. Streaming with Full Control (OpenAI):
            ----------------------------------------""");
        try {
            var openai = OpenAiStreamingChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName("gpt-4o-mini")
                    .build();
            
            // Handle both partial responses and errors
            openai.chat("Why is the sky blue? Answer in exactly one sentence.", 
                    onPartialResponseAndError(
                            System.out::print,  // Handle each token
                            error -> System.err.println("\nError: " + error.getMessage())  // Handle errors
                    ));
        } catch (Exception e) {
            System.out.println("OpenAI not available - check OPENAI_API_KEY");
        }
        
        System.out.println("""
            
            === Key Benefits ===
            1. No anonymous inner classes needed
            2. Clean, functional style with lambdas
            3. Choose simple (onPartialResponse) or with error handling (onPartialResponseAndError)
            4. Less boilerplate code
            """);
    }
}