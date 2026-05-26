package com.kousenit.demos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class StructuredOutputDemoTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parses_structured_output_into_record() throws Exception {
        String json =
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

        StructuredOutputDemo.CourseInquiry inquiry = mapper.readValue(json, StructuredOutputDemo.CourseInquiry.class);

        StructuredOutputDemo.validate(inquiry);

        assertThat(inquiry.topic()).contains("structured outputs");
        assertThat(inquiry.priority()).isEqualTo(StructuredOutputDemo.Priority.HIGH);
        assertThat(inquiry.needsFollowUp()).isTrue();
        assertThat(inquiry.actionItems()).hasSize(2);
    }

    @Test
    void validation_rejects_empty_action_items() {
        var inquiry = new StructuredOutputDemo.CourseInquiry(
                "Missing work", StructuredOutputDemo.Priority.MEDIUM, true, List.of());

        assertThatThrownBy(() -> StructuredOutputDemo.validate(inquiry))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("action item");
    }
}
