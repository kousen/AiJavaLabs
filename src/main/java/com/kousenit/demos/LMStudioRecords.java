package com.kousenit.demos;

import java.util.List;
import java.util.Map;

public class LMStudioRecords {

    public record ChatRequest(
            String model,
            List<Message> messages,
            double temperature,
            int maxTokens,
            boolean stream,
            List<Tool> tools,
            String toolChoice
    ) {
        // Constructor without tools for backward compatibility
        public ChatRequest(String model, List<Message> messages, double temperature, int maxTokens, boolean stream) {
            this(model, messages, temperature, maxTokens, stream, null, null);
        }
    }

    public record ChatCompletionResponse(
            String id,
            String object,
            long created,
            String model,
            List<Choice> choices,
            Usage usage,
            Map<String, Object> stats,
            String systemFingerprint
    ) {}

    public record Choice(
            int index,
            Message message,
            Object logprobs,
            String finishReason
    ) {}

    public record Message(
            String role,
            String content,
            String reasoning,
            List<ToolCall> toolCalls
    ) {}

    public record ToolCall(
            String id,
            String type,
            FunctionCall function
    ) {}

    public record FunctionCall(
            String name,
            String arguments
    ) {}

    public record Usage(
            int promptTokens,
            int completionTokens,
            int totalTokens
    ) {}

    public record Tool(
            String type,
            Function function
    ) {}

    public record Function(
            String name,
            String description,
            Map<String, Object> parameters
    ) {}

}
