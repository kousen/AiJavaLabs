package com.kousenit.demos;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.kousenit.demos.LMStudioRecords.*;

class LMStudioMCPTest {

    @Test
    void testMCPToolUse() {
        LMStudioService service = new LMStudioService();
        
        // Define tools explicitly - MCP tools need to be declared
        List<Tool> tools = List.of(
                new Tool("function", 
                    new Function(
                        "list_files",
                        "List files in a directory",
                        Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "path", Map.of("type", "string", "description", "Directory path")
                            ),
                            "required", List.of("path")
                        )
                    )
                ),
                new Tool("function",
                    new Function(
                        "web_search",
                        "Search the web for information",
                        Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "query", Map.of("type", "string", "description", "Search query")
                            ),
                            "required", List.of("query")
                        )
                    )
                )
        );
        
        String question = """
            Please use the web_search tool to search for "LM Studio MCP support".
            """;
        
        // Create request with explicit tools
        ChatCompletionResponse response = service.chat(
                new ChatRequest(
                        "qwen/qwen3-4b-thinking-2507",
                        List.of(
                                new Message("user", question, null, null)
                        ),
                        0.7,
                        -1,
                        false,
                        tools,  // Explicitly provide tools
                        "auto" // Allow model to decide when to use tools
                )
        );
        
        // Check if the model made any tool calls
        Choice choice = response.choices().getFirst();
        if (choice.message().toolCalls() != null && !choice.message().toolCalls().isEmpty()) {
            System.out.println("Tool calls made:");
            for (ToolCall toolCall : choice.message().toolCalls()) {
                System.out.println("Tool: " + toolCall.function().name());
                System.out.println("Arguments: " + toolCall.function().arguments());
            }
        } else {
            System.out.println("No tool calls made. Response:");
            System.out.println(choice.message().content());
        }
    }
}