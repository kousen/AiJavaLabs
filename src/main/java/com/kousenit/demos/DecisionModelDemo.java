package com.kousenit.demos;

import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.decision.request.ChoiceQuestion;
import dev.langchain4j.model.decision.request.DecisionRequest;
import dev.langchain4j.model.decision.request.ScaleQuestion;
import dev.langchain4j.model.decision.request.YesNoQuestion;
import dev.langchain4j.model.decision.response.ChoiceAnswer;
import dev.langchain4j.model.decision.response.DecisionResponse;
import dev.langchain4j.model.decision.response.ScaleAnswer;
import dev.langchain4j.model.decision.response.YesNoAnswer;
import dev.langchain4j.model.typesafe.TypeSafeDecisionModel;
import java.util.List;
import java.util.Objects;

/**
 * Demo of LangChain4j's experimental DecisionModel API.
 *
 * A decision model does not generate text. It answers typed questions about an
 * input and returns a probability for every option, so there is nothing to parse
 * and Java can apply thresholds directly. The model here is TypeSafe's Jev,
 * reached through OpenRouter with the same key as the chat demos.
 *
 * Compare with StructuredOutputDemo, which asks a chat model to fill in a record:
 * that gives you one answer, this gives you how sure the model is about each one.
 */
public class DecisionModelDemo {
    // TypeSafeDecisionModel appends "v1/systemone" itself, so no "/v1" here
    private static final String OPENROUTER_BASE_URL = "https://openrouter.ai/api";

    public static void main(String[] args) {
        String apiKey = Objects.requireNonNull(
                System.getenv("OPENROUTER_API_KEY"), "Set the OPENROUTER_API_KEY environment variable");

        DecisionModel model = TypeSafeDecisionModel.builder()
                .baseUrl(OPENROUTER_BASE_URL)
                .apiKey(apiKey)
                .modelName("typesafe/jev-1.13")
                .build();

        System.out.println("=== Decision Model Demo: triaging training inquiries ===\n");

        List<String> inquiries = List.of(
                """
                We have 40 developers moving from Java 8 to Java 21 next quarter and need \
                training before the migration starts. Can you send a quote this week?""",
                """
                Loved the Spring Boot course last year. Do you have anything on adding \
                AI features to our existing Spring applications?""",
                "Hi, just curious whether you ever teach in Europe.");

        for (String inquiry : inquiries) {
            triage(model, inquiry);
        }
    }

    private static void triage(DecisionModel model, String inquiry) {
        // All questions are answered against the same input in a single call
        DecisionRequest request = DecisionRequest.builder()
                .input(inquiry)
                .question(
                        "course",
                        ChoiceQuestion.builder()
                                .text("Which course does the inquiry ask about?")
                                .option("java", "Core Java, language features, migrating between Java versions")
                                .option("spring", "Spring Framework and Spring Boot")
                                .option("ai", "Integrating AI models and APIs into Java applications")
                                .option("other", "Anything else, or no specific course")
                                .build())
                .question(
                        "follow_up",
                        YesNoQuestion.from(
                                "Does the sender ask for a concrete next step, such as a quote, a date, or a call?"))
                .question(
                        "urgency",
                        ScaleQuestion.builder()
                                .text("How soon does the sender need an answer?")
                                .level("Whenever", "No timing mentioned")
                                .level("Soon", "Mentions a month or a quarter")
                                .level("Now", "Mentions this week or sooner")
                                .build())
                .build();

        DecisionResponse response = model.decide(request);

        ChoiceAnswer course = response.choice("course");
        YesNoAnswer followUp = response.yesNo("follow_up");
        ScaleAnswer urgency = response.scale("urgency");

        System.out.println("Inquiry: " + inquiry.replace('\n', ' '));
        System.out.printf("  course    : %s  %s%n", course.value(), course.probabilities());
        System.out.printf("  follow_up : %.2f probability of yes%n", followUp.probability());
        System.out.printf("  urgency   : %.2f on a 0-2 scale  %s%n", urgency.mean(), urgency.probabilities());

        // The probabilities drive decisions in Java. Thresholds are yours to tune per model.
        if (course.margin() < 0.2) {
            System.out.println("  -> Course unclear (top two options too close), route to a human");
        }
        if (followUp.isYes(0.7) && urgency.mean() >= 1.5) {
            System.out.println("  -> Asks for a next step and needs it now: reply today");
        } else if (followUp.isYes(0.7)) {
            System.out.println("  -> Asks for a next step: add to this week's follow-ups");
        } else {
            System.out.println("  -> No action requested: send the standard reply");
        }
        System.out.println();
    }
}
