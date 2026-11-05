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

    /** Builds the extractor for any chat model, so a test can drive it with a scripted one. */
    static InquiryExtractor extractor(ChatModel model) {
        return AiServices.builder(InquiryExtractor.class).chatModel(model).build();
    }

    // ---- Optional step: when the answer is one of several shapes ----
    //
    // A record gives you one shape with every field. Some answers are a choice between shapes:
    // a quote request has a head count, a scheduling request has dates, noise has neither.
    // Model that as a sealed interface. LangChain4j turns it into a JSON Schema with one object
    // per permitted record and a "type" tag, the model picks one, and Java gets an exhaustive
    // switch: add a variant and nextStep() stops compiling until you handle it.

    @Description("What the sender of a training inquiry actually wants")
    public sealed interface InquiryResult permits QuoteRequest, SchedulingRequest, Noise {}

    @Description("The sender wants a price for training a group")
    public record QuoteRequest(
            @JsonProperty(required = true) @Description("Course or topic, in a few words") String course,
            @JsonProperty(required = true) @Description("How many people would attend") int developers,
            @Description("When they need it, in their own words, if they said") String timeframe)
            implements InquiryResult {}

    @Description("The sender wants to arrange dates or a call, no price asked")
    public record SchedulingRequest(
            @JsonProperty(required = true) @Description("Course or topic, in a few words") String course,
            @Description("Dates or windows they mentioned, if any") String preferredDates)
            implements InquiryResult {}

    @Description("No concrete request: curiosity, thanks, or spam")
    public record Noise(@JsonProperty(required = true) @Description("One short reason") String reason)
            implements InquiryResult {}

    interface InquiryClassifier {
        InquiryResult classify(String message);
    }

    static InquiryClassifier classifier(ChatModel model) {
        return AiServices.builder(InquiryClassifier.class).chatModel(model).build();
    }

    /** The application's decision. Exhaustive over the sealed type, so no default branch and no surprises. */
    static String nextStep(InquiryResult result) {
        return switch (result) {
            case QuoteRequest(String course, int developers, String timeframe) ->
                "Send a %s quote for %d developers (%s)"
                        .formatted(course, developers, timeframe == null ? "no date given" : timeframe);
            case SchedulingRequest(String course, String dates) ->
                "Offer %s dates%s".formatted(course, dates == null ? "" : " around " + dates);
            case Noise(String reason) -> "Standard reply (" + reason + ")";
        };
    }

    public static void main(String[] args) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("OPENAI_API_KEY is not set. Set it before running this demo.");
            return;
        }

        ChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-6-luna")
                .supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
                .strictJsonSchema(true)
                .build();

        InquiryExtractor extractor = extractor(model);

        String message =
                """
                A student says: I finished the tool calling lab, but I am still
                confused about the difference between tool calling and structured
                outputs. Could you add a small example before tomorrow?
                """;

        CourseInquiry inquiry = extractor.extract(message);
        validate(inquiry);

        System.out.println(inquiry);

        // Optional step: one of several shapes, and an exhaustive switch over which one came back
        InquiryClassifier classifier = classifier(model);
        for (String text : List.of(
                "We have 40 developers moving from Java 8 to Java 21 next quarter. Can you send a quote this week?",
                "Loved the Spring Boot course. Could we find a couple of days in January for the AI one?",
                "Hi, just curious whether you ever teach in Europe.")) {
            InquiryResult result = classifier.classify(text);
            System.out.println("\n" + text + "\n  -> " + result + "\n  -> " + nextStep(result));
        }
    }
}
