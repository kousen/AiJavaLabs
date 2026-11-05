package com.kousenit;

import static com.kousenit.OllamaRecords.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OllamaServiceTest {
    private static final String MODEL = "gemma4";
    private final OllamaService service = new OllamaService();

    @Test
    @Tag(TestCategories.LOCAL)
    @Tag(TestCategories.QUICK)
    @Tag(TestCategories.DEMO)
    void chat_with_one_message() {
        OllamaChatResponse response = service.chat(MODEL, "Why is the sky blue?");

        System.out.println(response);
        assertThat(response.message().role()).isEqualTo("assistant");
        assertThat(response.message().content()).containsIgnoringCase("scattering");
        assertThat(response.createdAt())
                .as("created_at maps to createdAt via SNAKE_CASE")
                .isNotNull();
    }

    @Test
    @Tag(TestCategories.LOCAL)
    @Tag(TestCategories.QUICK)
    @Tag(TestCategories.DEMO)
    void each_request_is_stateless() {
        service.chat(MODEL, "My name is Inigo Montoya.");
        OllamaChatResponse second = service.chat(MODEL, "What's my name? Answer in one short sentence.");

        System.out.println(second.message().content());
        // The server kept nothing from the first call
        assertThat(second.message().content()).doesNotContain("Inigo");
    }

    @Test
    @Tag(TestCategories.LOCAL)
    @Tag(TestCategories.QUICK)
    @Tag(TestCategories.DEMO)
    void appending_the_reply_gives_the_model_a_memory() {
        List<OllamaMessage> conversation = new ArrayList<>();
        conversation.add(OllamaMessage.user("My name is Inigo Montoya."));
        OllamaChatResponse first = service.chat(new OllamaChatRequest(MODEL, conversation, false));

        conversation.add(first.message()); // the assistant's reply goes into the list
        conversation.add(OllamaMessage.user("What's my name? Answer in one short sentence."));
        OllamaChatResponse second = service.chat(new OllamaChatRequest(MODEL, conversation, false));

        System.out.println(second.message().content());
        assertThat(second.message().content()).contains("Inigo");
    }

    @Test
    @Tag(TestCategories.LOCAL)
    @Tag(TestCategories.SLOW)
    void streaming_tokens_arrive_one_at_a_time() {
        List<String> tokens = new ArrayList<>();
        String full = service.chatStreaming(
                MODEL, List.of(OllamaMessage.user("Count from 1 to 5, digits only.")), tokens::add);

        System.out.println(tokens.size() + " tokens: " + full);
        assertThat(tokens).hasSizeGreaterThan(1);
        assertThat(String.join("", tokens)).isEqualTo(full).containsSubsequence("1", "2", "3", "4", "5");
    }

    @Test
    @Tag(TestCategories.LOCAL)
    @Tag(TestCategories.SLOW)
    void an_image_rides_along_on_the_message() {
        var question = OllamaMessage.user(
                "This is a screenshot from my terminal. What went wrong, and what should I check first?",
                Path.of("src/main/resources/stack_trace.png"));
        OllamaChatResponse response = service.chat(new OllamaChatRequest(MODEL, List.of(question), false));

        System.out.println(response.message().content());
        assertThat(response.message().content()).containsAnyOf("connect", "Connect", "refused", "11434", "Ollama");
    }

    @Test
    @Tag(TestCategories.LOCAL)
    @Tag(TestCategories.QUICK)
    void generate_is_the_single_prompt_aside() {
        OllamaGenerateResponse response = service.generate(MODEL, "Why is the sky blue?");

        assertThat(response.response()).containsIgnoringCase("scattering");
        assertThat(response.createdAt()).isNotNull();
    }
}
