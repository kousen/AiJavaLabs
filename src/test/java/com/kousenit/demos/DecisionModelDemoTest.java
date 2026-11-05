package com.kousenit.demos;

import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.decision.request.ChoiceQuestion;
import dev.langchain4j.model.decision.request.DecisionRequest;
import dev.langchain4j.model.decision.response.ChoiceAnswer;
import dev.langchain4j.model.decision.response.DecisionResponse;
import dev.langchain4j.model.decision.response.ScaleAnswer;
import dev.langchain4j.model.decision.response.YesNoAnswer;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Exercises DecisionModelDemo without Jev or a key. The request test pins the
 * question keys the response lookups depend on; the end-to-end test proves the
 * request keys and the response lookups still agree after an edit; the rest
 * pin the thresholds, which are the teaching point.
 */
class DecisionModelDemoTest {

    @Test
    void triage_request_asks_the_three_questions_the_code_reads_back() {
        DecisionRequest request = DecisionModelDemo.triageRequest("Can you quote Java 21 training?");

        assertThat(request.questions()).containsOnlyKeys("course", "follow_up", "urgency");
        assertThat(((ChoiceQuestion) request.questions().get("course")).options())
                .containsKeys("java", "spring", "ai", "other");
    }

    @Test
    void triage_runs_end_to_end_against_a_scripted_decision_model() {
        DecisionModel model = request -> DecisionResponse.builder()
                .answer("course", choice("java", 0.9))
                .answer("follow_up", YesNoAnswer.from(0.95))
                .answer(
                        "urgency",
                        ScaleAnswer.builder()
                                .mean(1.8)
                                .probabilities(List.of(0.0, 0.2, 0.8))
                                .build())
                .build();

        // Throws if a response key no longer matches a request key
        DecisionModelDemo.triage(model, "40 developers, Java 8 to 21, quote this week?");
    }

    @Test
    void clear_course_and_urgent_follow_up_means_reply_today() {
        List<String> actions = DecisionModelDemo.actions(
                choice("java", 0.9),
                YesNoAnswer.from(0.95),
                ScaleAnswer.builder()
                        .mean(1.8)
                        .probabilities(List.of(0.0, 0.2, 0.8))
                        .build());

        assertThat(actions).singleElement().asString().contains("reply today");
    }

    @Test
    void follow_up_without_urgency_goes_on_this_weeks_list() {
        List<String> actions = DecisionModelDemo.actions(
                choice("ai", 0.8),
                YesNoAnswer.from(0.75),
                ScaleAnswer.builder()
                        .mean(0.9)
                        .probabilities(List.of(0.3, 0.5, 0.2))
                        .build());

        assertThat(actions).singleElement().asString().contains("this week's follow-ups");
    }

    @Test
    void close_course_probabilities_add_a_human_routing_action() {
        ChoiceAnswer unclear = ChoiceAnswer.builder()
                .value("spring")
                .probabilities(Map.of("java", 0.40, "spring", 0.45, "ai", 0.10, "other", 0.05))
                .build();
        List<String> actions = DecisionModelDemo.actions(
                unclear,
                YesNoAnswer.from(0.2),
                ScaleAnswer.builder()
                        .mean(0.1)
                        .probabilities(List.of(0.9, 0.1, 0.0))
                        .build());

        assertThat(actions).hasSize(2);
        assertThat(actions.get(0)).contains("route to a human");
        assertThat(actions.get(1)).contains("standard reply");
    }

    private static ChoiceAnswer choice(String winner, double probability) {
        double rest = (1.0 - probability) / 3;
        Map<String, Double> probabilities =
                new java.util.HashMap<>(Map.of("java", rest, "spring", rest, "ai", rest, "other", rest));
        probabilities.put(winner, probability);
        return ChoiceAnswer.builder().value(winner).probabilities(probabilities).build();
    }
}
