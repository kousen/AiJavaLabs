package com.kousenit.demos;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.AiServices;
import java.util.List;

/**
 * Demonstrates extracting structured data from unstructured text.
 *
 * <p>When JSON Schema support is enabled, LangChain4j can generate a schema
 * from the return type, ask the model for matching JSON, parse the output, and
 * return a Java record instead of a String.
 */
public class StructuredOutputDemo {

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

    @Description("A structured summary of a student or customer request")
    public record CourseInquiry(
            @JsonProperty(required = true) @Description("Short topic label") String topic,
            @JsonProperty(required = true) Priority priority,
            @JsonProperty(required = true) @Description("Whether a human should follow up") boolean needsFollowUp,
            @JsonProperty(required = true) @Description("Concrete next actions") List<String> actionItems) {}

    interface InquiryExtractor {
        CourseInquiry extract(String message);
    }

    public static void validate(CourseInquiry inquiry) {
        if (inquiry.topic() == null || inquiry.topic().isBlank()) {
            throw new IllegalArgumentException("topic is required");
        }
        if (inquiry.priority() == null) {
            throw new IllegalArgumentException("priority is required");
        }
        if (inquiry.actionItems() == null || inquiry.actionItems().isEmpty()) {
            throw new IllegalArgumentException("at least one action item is required");
        }
    }

    public static void main(String[] args) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("OPENAI_API_KEY is not set. Set it before running this demo.");
            return;
        }

        ChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-5-nano")
                .temperature(0.0)
                .supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
                .strictJsonSchema(true)
                .build();

        InquiryExtractor extractor =
                AiServices.builder(InquiryExtractor.class).chatModel(model).build();

        String message =
                """
                A student says: I finished the tool calling lab, but I am still
                confused about the difference between tool calling and structured
                outputs. Could you add a small example before tomorrow?
                """;

        CourseInquiry inquiry = extractor.extract(message);
        validate(inquiry);

        System.out.println(inquiry);
    }
}
