package com.kousenit.demos;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

public class LMStudioToolsLC4jTest {

    // Define tools that LangChain4j will handle
    public static class SearchTools {
        
        @Tool("Search the web for information")
        public String webSearch(String query) {
            // In a real app, this would call an actual search API
            // For demo, we'll simulate MCP-like functionality
            return """
                    Simulated search results for: %s
                    1. LM Studio supports MCP through configuration
                    2. MCP allows external tool integration
                    3. Models need explicit tool support""".formatted(query);
        }
        
        @Tool("List files in a directory")
        public List<String> listFiles(String path) {
            // Simulate file listing
            return List.of("file1.txt", "file2.java", "README.md");
        }
        
        @Tool("Get current date and time")
        public String getCurrentTime() {
            return LocalDateTime.now().toString();
        }
    }
    
    // Define the AI service interface
    interface Assistant {
        String chat(String message);
    }
    
    @Test
    void testLangChain4jToolsWithLMStudio() {
        // Configure LM Studio as OpenAI-compatible endpoint
        var model = OpenAiChatModel.builder()
                .baseUrl("http://localhost:1234/v1")
                .apiKey("not-needed")  // LM Studio doesn't require API key
                .modelName("qwen/qwen3-4b-thinking-2507")  // Model with tool support
                .temperature(0.7)
                .logRequests(true)
                .logResponses(true)
                .build();
        
        // Create AI service with tools
        var assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .tools(new SearchTools())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
        
        // Test tool usage
        System.out.println("Testing LangChain4j tools with LM Studio:\n");
        
        // This will automatically use the webSearch tool
        String response1 = assistant.chat("Search for information about LM Studio MCP support");
        System.out.println("Response 1: " + response1);
        
        // This will use the getCurrentTime tool
        String response2 = assistant.chat("What time is it?");
        System.out.println("\nResponse 2: " + response2);
        
        // This will use the listFiles tool
        String response3 = assistant.chat("List files in the current directory");
        System.out.println("\nResponse 3: " + response3);
    }
    
    @Test
    void testComplexToolInteraction() {
        var model = OpenAiChatModel.builder()
                .baseUrl("http://localhost:1234/v1")
                .apiKey("not-needed")
                .modelName("qwen/qwen3-4b-thinking-2507")
                .temperature(0.3)
                .build();
        
        var assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .tools(new SearchTools())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
        
        // Complex request that might use multiple tools
        String response = assistant.chat(
                """
                First tell me what time it is,
                then search for 'Java tool calling',
                and finally list the files in the
                current directory. Summarize all
                the results.
                """
        );
        
        System.out.println("Complex interaction response:\n" + response);
    }
}