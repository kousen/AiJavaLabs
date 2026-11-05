package com.kousenit.demos;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.output.OutputParsingException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Drives the structured-output AI Service with a scripted model, so these tests
 * need no API key. They exercise the LangChain4j path (schema generation, JSON
 * parsing into the record), not Jackson on its own: the Nov 4, 2026 class failure
 * lived in the framework layer, and a test that bypasses it cannot catch that.
 */
class StructuredOutputDemoTest {

    private static final String VALID_JSON =
            """
            {
              "topic": "Tool calling versus structured outputs",
              "priority": "HIGH",
              "needsFollowUp": true,
              "actionItems": [
                "Add a small structured-output demo",
                "Explain when to use tools versus JSON records"
              ]
            }
            """;

    @Test
    void service_sends_a_json_schema_with_every_field_required() {
        var model = new ScriptedModel(VALID_JSON);

        StructuredOutputDemo.extractor(model).extract("any message");

        assertThat(model.request.responseFormat().type()).isEqualTo(ResponseFormatType.JSON);
        JsonObjectSchema root =
                (JsonObjectSchema) model.request.responseFormat().jsonSchema().rootElement();
        assertThat(root.required()).containsExactlyInAnyOrder("topic", "priority", "needsFollowUp", "actionItems");
    }

    @Test
    void model_json_becomes_a_record() {
        var model = new ScriptedModel(VALID_JSON);

        StructuredOutputDemo.CourseInquiry inquiry =
                StructuredOutputDemo.extractor(model).extract("any message");
        StructuredOutputDemo.validate(inquiry);

        assertThat(inquiry.topic()).contains("structured outputs");
        assertThat(inquiry.priority()).isEqualTo(StructuredOutputDemo.Priority.HIGH);
        assertThat(inquiry.needsFollowUp()).isTrue();
        assertThat(inquiry.actionItems()).hasSize(2);
    }

    @Test
    void json_wrapped_in_markdown_fences_is_still_parsed() {
        var model = new ScriptedModel("```json\n" + VALID_JSON + "\n```");

        StructuredOutputDemo.CourseInquiry inquiry =
                StructuredOutputDemo.extractor(model).extract("any message");

        assertThat(inquiry.priority()).isEqualTo(StructuredOutputDemo.Priority.HIGH);
    }

    @Test
    void missing_required_field_is_rejected_by_the_framework_not_by_validate() {
        var model = new ScriptedModel(
                """
                {"topic": "Java 21 migration", "priority": "MEDIUM", "needsFollowUp": false}
                """);

        // @JsonProperty(required = true) does two jobs: it puts the field in the schema's
        // required list, and Jackson enforces it when parsing the reply. validate() is for
        // what a schema cannot say, such as "the list must not be empty".
        assertThatThrownBy(() -> StructuredOutputDemo.extractor(model).extract("any message"))
                .isInstanceOf(OutputParsingException.class)
                .rootCause()
                .hasMessageContaining("Missing required creator property 'actionItems'");
    }

    @Test
    void unknown_enum_value_fails_inside_the_framework() {
        var model = new ScriptedModel(
                """
                {"topic": "x", "priority": "URGENT", "needsFollowUp": true, "actionItems": ["call back"]}
                """);

        assertThatThrownBy(() -> StructuredOutputDemo.extractor(model).extract("any message"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("URGENT");
    }

    @Test
    void validation_rejects_empty_action_items() {
        var inquiry = new StructuredOutputDemo.CourseInquiry(
                "Missing work", StructuredOutputDemo.Priority.MEDIUM, true, List.of());

        assertThatThrownBy(() -> StructuredOutputDemo.validate(inquiry))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("action item");
    }

    /** Claims JSON Schema support (as the OpenAI model in main does) and replies with fixed text. */
    static class ScriptedModel implements ChatModel {
        ChatRequest request;
        private final String reply;

        ScriptedModel(String reply) {
            this.reply = reply;
        }

        @Override
        public Set<Capability> supportedCapabilities() {
            return Set.of(RESPONSE_FORMAT_JSON_SCHEMA);
        }

        @Override
        public ChatResponse chat(ChatRequest request) {
            this.request = request;
            return ChatResponse.builder().aiMessage(AiMessage.from(reply)).build();
        }
    }
}
